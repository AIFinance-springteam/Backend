package AIFinance.demo.trip.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.global.exception.SplitErrorCode;
import AIFinance.demo.global.security.SecurityUtil;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptAnalysisStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.trip.dto.DashboardAmountResponse;
import AIFinance.demo.trip.dto.DashboardDifferenceResponse;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;
    private final TripMemberRepository tripMemberRepository;

    public DashboardAmountResponse getTotalExpense(Long tripId) {
        return DashboardAmountResponse.of(calculateTotalExpense(tripId));
    }

    public DashboardAmountResponse getMyPayments(Long tripId) {
        Long tripMemberId = resolveTripMemberId(tripId);
        return DashboardAmountResponse.of(calculatePayments(tripId, tripMemberId));
    }

    public DashboardAmountResponse getMyShares(Long tripId) {
        Long tripMemberId = resolveTripMemberId(tripId);
        return DashboardAmountResponse.of(calculateShares(tripId, tripMemberId));
    }

    public DashboardDifferenceResponse getExpectedDifference(Long tripId) {
        Long tripMemberId = resolveTripMemberId(tripId);

        long myPayments = calculatePayments(tripId, tripMemberId);
        long myShares = calculateShares(tripId, tripMemberId);
        boolean isFinal = !hasUnassignedItems(tripId);

        return DashboardDifferenceResponse.of(myPayments, myShares, isFinal);
    }

    private Long resolveTripMemberId(Long tripId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return tripMemberRepository.findByTrip_IdAndUser_Id(tripId, userId)
                .map(TripMember::getId)
                .orElseThrow(() -> new GeneralException(SplitErrorCode.MEMBER_NOT_IN_TRIP));
    }

    private long calculateTotalExpense(Long tripId) {
        return getValidReceipts(tripId).stream()
                .mapToLong(receipt -> receipt.getTotalAmount() == null ? 0L : receipt.getTotalAmount())
                .sum();
    }

    private long calculatePayments(Long tripId, Long tripMemberId) {
        return getValidReceipts(tripId).stream()
                .filter(receipt -> receipt.getPayerMember() != null
                        && receipt.getPayerMember().getId().equals(tripMemberId))
                .mapToLong(receipt -> receipt.getTotalAmount() == null ? 0L : receipt.getTotalAmount())
                .sum();
    }

    private long calculateShares(Long tripId, Long tripMemberId) {
        List<Long> itemIds = getValidItemIds(tripId);
        if (itemIds.isEmpty()) {
            return 0L;
        }

        return itemShareRepository.findByItem_IdInAndTripMember_Id(itemIds, tripMemberId).stream()
                .mapToLong(share -> share.getShareAmount())
                .sum();
    }

    private boolean hasUnassignedItems(Long tripId) {
        List<Long> itemIds = getValidItemIds(tripId);
        return itemIds.stream().anyMatch(itemId -> !itemShareRepository.existsByItem_Id(itemId));
    }

    private List<Long> getValidItemIds(Long tripId) {
        List<Long> receiptIds = getValidReceipts(tripId).stream()
                .map(Receipt::getId)
                .toList();

        if (receiptIds.isEmpty()) {
            return List.of();
        }

        return receiptItemRepository.findByReceipt_IdIn(receiptIds).stream()
                .map(ReceiptItem::getId)
                .toList();
    }

    private List<Receipt> getValidReceipts(Long tripId) {
        return receiptRepository.findByTrip_Id(tripId).stream()
                .filter(receipt -> receipt.getStatus() != ReceiptStatus.DELETED)
                .filter(receipt -> receipt.getAnalysisStatus() == ReceiptAnalysisStatus.SUCCESS)
                .toList();
    }
}