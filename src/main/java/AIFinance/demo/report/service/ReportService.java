package AIFinance.demo.report.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.report.dto.ReportResponse;
import AIFinance.demo.report.exception.ReportErrorCode;
import AIFinance.demo.report.exception.ReportException;
import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.settlement.repository.SettlementRepository;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private static final String UNCATEGORIZED = "OTHER";

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;
    private final SettlementRepository settlementRepository;
    private final ReportAnalysisProvider reportAnalysisProvider;

    public ReportResponse getReport(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND));

        if (tripMemberRepository.findByTrip_IdAndUser_Id(tripId, userId).isEmpty()) {
            throw new SettlementException(SettlementErrorCode.TRIP_MEMBER_REQUIRED);
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }

        Settlement settlement = settlementRepository.findByTrip_Id(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));
        if (settlement.getStatus() != SettlementStatus.COMPLETED || settlement.getTotalAmount() == null) {
            throw new ReportException(ReportErrorCode.REPORT_DATA_INCONSISTENT);
        }

        List<TripMember> members = tripMemberRepository.findAllByTrip_Id(tripId).stream()
                .sorted(Comparator.comparing(TripMember::getId))
                .toList();
        List<Receipt> receipts = receiptRepository.findAllByTrip_IdAndStatus(tripId, ReceiptStatus.CONFIRMED);
        List<ReceiptItem> items = getItems(receipts);
        List<ItemShare> shares = getShares(items);

        long receiptTotalAmount = receipts.stream()
                .map(Receipt::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        long totalAmount = settlement.getTotalAmount();
        long averagePerPerson = members.isEmpty() ? 0L : totalAmount / members.size();
        ReportResponse.Summary summary = new ReportResponse.Summary(totalAmount, averagePerPerson, receipts.size());
        List<ReportResponse.CategoryExpense> categories = calculateCategories(items, totalAmount);
        List<ReportResponse.DailyExpense> dailyExpenses = calculateDailyExpenses(receipts);
        List<ReportResponse.MemberExpense> memberExpenses = calculateMemberExpenses(members, receipts, shares);
        validateAmounts(totalAmount, receiptTotalAmount, items, shares, categories, memberExpenses);
        ReportAnalysisInput analysisInput = new ReportAnalysisInput(summary, categories, dailyExpenses);

        String content;
        try {
            content = reportAnalysisProvider.analyze(analysisInput);
            if (content == null || content.isBlank()) {
                content = ReportAnalysisText.generate(analysisInput);
            }
        } catch (RuntimeException ignored) {
            content = ReportAnalysisText.generate(analysisInput);
        }

        return new ReportResponse(
                trip.getId(),
                trip.getName(),
                summary,
                categories,
                dailyExpenses,
                memberExpenses,
                new ReportResponse.AiAnalysis(content, LocalDateTime.now())
        );
    }

    private List<ReceiptItem> getItems(List<Receipt> receipts) {
        List<Long> receiptIds = receipts.stream().map(Receipt::getId).toList();
        return receiptIds.isEmpty() ? List.of() : receiptItemRepository.findByReceipt_IdIn(receiptIds);
    }

    private List<ItemShare> getShares(List<ReceiptItem> items) {
        List<Long> itemIds = items.stream().map(ReceiptItem::getId).toList();
        return itemIds.isEmpty() ? List.of() : itemShareRepository.findByItem_IdIn(itemIds);
    }

    private List<ReportResponse.CategoryExpense> calculateCategories(List<ReceiptItem> items, long totalAmount) {
        Map<String, Long> amounts = items.stream().collect(Collectors.groupingBy(
                item -> normalizeCategory(item.getCategory()),
                Collectors.summingLong(ReceiptItem::getSettlementAmount)
        ));

        return amounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0L)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(entry -> new ReportResponse.CategoryExpense(
                        entry.getKey(),
                        categoryName(entry.getKey()),
                        entry.getValue(),
                        calculatePercentage(entry.getValue(), totalAmount)
                ))
                .toList();
    }

    private int calculatePercentage(long amount, long totalAmount) {
        if (totalAmount == 0L) {
            return 0;
        }
        BigInteger numerator = BigInteger.valueOf(amount)
                .multiply(BigInteger.valueOf(100L))
                .add(BigInteger.valueOf(totalAmount / 2L));
        return numerator.divide(BigInteger.valueOf(totalAmount)).intValue();
    }

    private List<ReportResponse.DailyExpense> calculateDailyExpenses(List<Receipt> receipts) {
        Map<LocalDate, Long> amounts = receipts.stream()
                .filter(receipt -> receipt.getPaidAt() != null && receipt.getTotalAmount() != null)
                .collect(Collectors.groupingBy(
                        receipt -> receipt.getPaidAt().toLocalDate(),
                        Collectors.summingLong(Receipt::getTotalAmount)
                ));

        return amounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ReportResponse.DailyExpense(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<ReportResponse.MemberExpense> calculateMemberExpenses(
            List<TripMember> members,
            List<Receipt> receipts,
            List<ItemShare> shares
    ) {
        Map<Long, Long> payments = receipts.stream()
                .filter(receipt -> receipt.getPayerMember() != null)
                .collect(Collectors.groupingBy(
                        receipt -> receipt.getPayerMember().getId(),
                        Collectors.summingLong(receipt -> Objects.requireNonNullElse(receipt.getTotalAmount(), 0L))
                ));
        Map<Long, Long> burdens = shares.stream().collect(Collectors.groupingBy(
                share -> share.getTripMember().getId(),
                Collectors.summingLong(ItemShare::getShareAmount)
        ));

        return members.stream()
                .map(member -> new ReportResponse.MemberExpense(
                        member.getId(),
                        member.getUser().getNickname(),
                        payments.getOrDefault(member.getId(), 0L),
                        burdens.getOrDefault(member.getId(), 0L)
                ))
                .toList();
    }

    private void validateAmounts(
            long settlementTotalAmount,
            long receiptTotalAmount,
            List<ReceiptItem> items,
            List<ItemShare> shares,
            List<ReportResponse.CategoryExpense> categories,
            List<ReportResponse.MemberExpense> memberExpenses
    ) {
        long itemTotalAmount = items.stream().mapToLong(ReceiptItem::getSettlementAmount).sum();
        long shareTotalAmount = shares.stream().mapToLong(ItemShare::getShareAmount).sum();
        long paidTotalAmount = memberExpenses.stream().mapToLong(ReportResponse.MemberExpense::paidAmount).sum();
        long memberShareTotalAmount = memberExpenses.stream().mapToLong(ReportResponse.MemberExpense::shareAmount).sum();
        long categoryTotalAmount = categories.stream().mapToLong(ReportResponse.CategoryExpense::amount).sum();

        if (settlementTotalAmount != receiptTotalAmount
                || settlementTotalAmount != itemTotalAmount
                || settlementTotalAmount != shareTotalAmount
                || settlementTotalAmount != paidTotalAmount
                || settlementTotalAmount != memberShareTotalAmount
                || settlementTotalAmount != categoryTotalAmount) {
            throw new ReportException(ReportErrorCode.REPORT_DATA_INCONSISTENT);
        }
    }

    private String normalizeCategory(String category) {
        return category == null || category.isBlank() ? UNCATEGORIZED : category.trim().toUpperCase(Locale.ROOT);
    }

    private String categoryName(String category) {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("ACCOMMODATION", "숙박");
        names.put("FOOD", "식비");
        names.put("TRANSPORTATION", "교통");
        names.put("TRANSPORT", "교통");
        names.put("SHOPPING", "쇼핑");
        names.put("ACTIVITY", "관광·활동");
        names.put("ADDITIONAL_COST", "추가 비용");
        names.put(UNCATEGORIZED, "기타");
        return names.getOrDefault(category, category);
    }
}
