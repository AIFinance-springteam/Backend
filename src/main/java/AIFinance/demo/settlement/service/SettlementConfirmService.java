package AIFinance.demo.settlement.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptDuplicateStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.settlement.dto.SettlementCheckResponse;
import AIFinance.demo.settlement.dto.SettlementResponse;
import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.settlement.repository.SettlementRepository;
import AIFinance.demo.settlement.repository.SettlementTransferRepository;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementConfirmService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementTransferRepository settlementTransferRepository;
    private final SettlementCheckService settlementCheckService;
    private final SettlementTransferCalculator transferCalculator;

    @Transactional
    public SettlementResponse.SettlementConfirmed confirmSettlement(Long userId, Long tripId) {
        Trip trip = getTripForUpdate(tripId);

        validateOwner(trip, userId);
        validateNotConfirmed(tripId);
        validateTripStatus(trip);

        SettlementCheckResponse.Result checkResult = settlementCheckService.checkSettlement(userId, tripId);

        if (!checkResult.readyToConfirm()) {
            throw new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_READY);
        }

        List<Receipt> receipts = getSettlementReceipts(tripId);
        List<ReceiptItem> items = getReceiptItems(receipts);
        List<ItemShare> shares = getItemShares(items);
        List<TripMember> members = tripMemberRepository.findAllByTrip_IdAndStatus(tripId, TripMemberStatus.ACTIVE);

        TripMember confirmer = findConfirmer(members, userId);

        Settlement settlement = settlementRepository.save(Settlement.create(trip, confirmer, checkResult.summary().totalAmount()));

        List<SettlementTransfer> transfers = transferCalculator.calculate(settlement, members, receipts, shares);

        settlementTransferRepository.saveAll(transfers);

        receipts.forEach(Receipt::confirm);
        trip.startSettlement();

        return SettlementResponse.SettlementConfirmed.from(settlement, transfers.size());

    }

    private Trip getTripForUpdate(Long tripId) {
        return tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND));
    }

    private List<Receipt> getSettlementReceipts(Long tripId) {
        return receiptRepository.findAllByTrip_IdAndStatusNot(tripId, ReceiptStatus.DELETED)
                .stream()
                .filter(receipt -> receipt.getDuplicateStatus() != ReceiptDuplicateStatus.DUPLICATE)
                .toList();
    }

    private List<ReceiptItem> getReceiptItems(List<Receipt> receipts) {
        List<Long> receiptIds = receipts.stream().map(Receipt::getId).toList();

        return receiptIds.isEmpty() ? List.of() : receiptItemRepository.findByReceipt_IdIn(receiptIds);
    }

    private  List<ItemShare> getItemShares(List<ReceiptItem> items) {
        List<Long> itemIds = items.stream().map(ReceiptItem::getId).toList();

        return itemIds.isEmpty() ? List.of() : itemShareRepository.findByItem_IdIn(itemIds);
    }

    private TripMember findConfirmer(List<TripMember> members, Long userId) {
        return members.stream()
                .filter(member -> Objects.equals(member.getUser().getId(), userId))
                .findFirst()
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_OWNER_REQUIRED));
    }

    private void validateOwner(Trip trip, Long userId) {
        if (!Objects.equals(trip.getOwner().getId(), userId)) {
            throw new SettlementException(SettlementErrorCode.TRIP_OWNER_REQUIRED);
        }
    }

    private void validateNotConfirmed(Long tripId) {
        if (settlementRepository.existsByTrip_Id(tripId)) {
            throw new SettlementException(SettlementErrorCode.SETTLEMENT_ALREADY_CONFIRMED);
        }
    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }
    }
}
