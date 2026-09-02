package AIFinance.demo.settlement.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptAnalysisStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptDuplicateStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.settlement.dto.SettlementCheckResponse;
import AIFinance.demo.settlement.dto.enums.SettlementCheckIssueType;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementCheckService {

    private final TripRepository tripRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;

    public SettlementCheckResponse.Result checkSettlement(Long userId, Long tripId) {
        Trip trip = getTrip(tripId);

        validateOwner(trip, userId);
        validateTripStatus(trip);

        List<Receipt> receipts = getSettlementReceipts(tripId);
        List<ReceiptItem> items = getReceiptItems(receipts);
        List<ItemShare> shares = getItemShares(items);

        Map<Long, List<ReceiptItem>> itemsByReceiptId = groupItemsByReceiptId(items);

        Map<Long, List<ItemShare>> sharesByItemId = groupSharesByItemId(shares);

        List<SettlementCheckResponse.Issue> issues = new ArrayList<>();

        checkReceiptIssues(receipts, issues);
        checkItemIssues(items, sharesByItemId, issues);
        checkReceiptAmounts(receipts, itemsByReceiptId, issues);

        long totalAmount = receipts.stream().map(Receipt::getTotalAmount).filter(Objects::nonNull).mapToLong(Long::longValue).sum();

        return SettlementCheckResponse.Result.of(tripId, receipts.size(), totalAmount, issues);
    }

    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND));
    }

    private List<Receipt> getSettlementReceipts(Long tripId) {
        return receiptRepository
                .findAllByTrip_IdAndStatusNot(tripId, ReceiptStatus.DELETED)
                .stream()
                .filter(receipt -> receipt.getDuplicateStatus() != ReceiptDuplicateStatus.DUPLICATE)
                .toList();
    }

    private List<ReceiptItem> getReceiptItems(List<Receipt> receipts) {
        List<Long> receiptIds = receipts.stream().map(Receipt::getId).toList();

        if (receiptIds.isEmpty()) {
            return List.of();
        }

        return receiptItemRepository.findByReceipt_IdIn(receiptIds);
    }

    private List<ItemShare> getItemShares(List<ReceiptItem> items) {
        List<Long> itemIds = items.stream().map(ReceiptItem::getId).toList();

        if (itemIds.isEmpty()) {
            return List.of();
        }

        return itemShareRepository.findByItem_IdIn(itemIds);
    }

    private Map<Long, List<ReceiptItem>> groupItemsByReceiptId(List<ReceiptItem> items) {
        return items.stream().collect(Collectors.groupingBy(item -> item.getReceipt().getId()));
    }

    private Map<Long, List<ItemShare>> groupSharesByItemId(List<ItemShare> shares) {
        return shares.stream().collect(Collectors.groupingBy(share -> share.getItem().getId()));
    }

    private void checkReceiptIssues(List<Receipt> receipts, List<SettlementCheckResponse.Issue> issues) {
        for (Receipt receipt : receipts) {

            if (receipt.getAnalysisStatus() != ReceiptAnalysisStatus.SUCCESS) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.ANALYSIS_INCOMPLETE, receipt.getId(), null, "분석이 완료되지 않은 영수증입니다."));
            }

            if (receipt.getPayerMember() == null) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.PAYER_UNASSIGNED, receipt.getId(), null, "결제자가 지정되지 않은 영수증입니다."));
            }

            if (receipt.getDuplicateStatus() == ReceiptDuplicateStatus.PENDING) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.DUPLICATE_REVIEW_REQUIRED, receipt.getId(), null, "중복 여부 확인이 필요한 영수증입니다."));
            }
        }
    }

    private void checkItemIssues(List<ReceiptItem> items, Map<Long, List<ItemShare>> sharesByItemId, List<SettlementCheckResponse.Issue> issues) {
        for (ReceiptItem item : items) {
            List<ItemShare> itemShares = sharesByItemId.getOrDefault(item.getId(), List.of());

            if (itemShares.isEmpty()) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.ITEM_SHARE_UNASSIGNED, item.getReceipt().getId(), item.getId(), "부담자가 지정되지 않은 상품입니다."));
                continue;
            }

            long shareTotal = itemShares.stream().mapToLong(ItemShare::getShareAmount).sum();

            if (shareTotal != item.getSettlementAmount()) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.SHARE_AMOUNT_MISMATCH, item.getReceipt().getId(), item.getId(), "상품 정산 금액과 부담 금액 합계가 일치하지 않습니다."));
            }
        }
    }

    private void checkReceiptAmounts(List<Receipt> receipts, Map<Long, List<ReceiptItem>> itemsByReceiptId, List<SettlementCheckResponse.Issue> issues) {
        for (Receipt receipt : receipts) {
            List<ReceiptItem> receiptItems = itemsByReceiptId.getOrDefault(receipt.getId(), List.of());

            long itemTotal = receiptItems.stream().mapToLong(ReceiptItem::getSettlementAmount).sum();

            if (!Objects.equals(receipt.getTotalAmount(), itemTotal)) {
                issues.add(SettlementCheckResponse.Issue.of(SettlementCheckIssueType.RECEIPT_AMOUNT_MISMATCH, receipt.getId(), null, "영수증 총액과 상품 정산 금액 합계가 일치하지 않습니다."));
            }
        }
    }

    private void validateOwner(Trip trip, Long userId) {
        if (!Objects.equals(trip.getOwner().getId(), userId)) {
            throw new SettlementException(SettlementErrorCode.TRIP_OWNER_REQUIRED);
        }
    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }
    }
}
