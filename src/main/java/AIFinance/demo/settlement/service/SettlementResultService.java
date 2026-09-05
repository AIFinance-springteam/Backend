package AIFinance.demo.settlement.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.settlement.dto.SettlementResultResponse;
import AIFinance.demo.settlement.entity.Settlement;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementResultService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementTransferRepository settlementTransferRepository;

    public SettlementResultResponse.Result getSettlementResult(Long userId, Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND)
                );

        boolean isActiveMember =
                tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(
                        tripId,
                        userId,
                        TripMemberStatus.ACTIVE
                );

        if (!isActiveMember) {
            throw new SettlementException(SettlementErrorCode.TRIP_MEMBER_REQUIRED);
        }

        if (trip.getStatus() != TripStatus.SETTLING
                && trip.getStatus() != TripStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }

        Settlement settlement = settlementRepository.findByTrip_Id(tripId)
                .orElseThrow(() ->
                        new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND)
                );

        List<TripMember> members =
                tripMemberRepository.findAllByTrip_Id(tripId)
                        .stream()
                        .sorted(Comparator.comparing(TripMember::getId))
                        .toList();

        List<Receipt> receipts =
                receiptRepository.findAllByTrip_IdAndStatus(
                        tripId,
                        ReceiptStatus.CONFIRMED
                );

        List<Long> receiptIds = receipts.stream()
                .map(Receipt::getId)
                .toList();

        List<ReceiptItem> items = receiptIds.isEmpty()
                ? List.of()
                : receiptItemRepository.findByReceipt_IdIn(receiptIds);

        List<Long> itemIds = items.stream()
                .map(ReceiptItem::getId)
                .toList();

        List<ItemShare> shares = itemIds.isEmpty()
                ? List.of()
                : itemShareRepository.findByItem_IdIn(itemIds);

        Map<Long, Long> payments = receipts.stream()
                .collect(Collectors.groupingBy(
                        receipt -> receipt.getPayerMember().getId(),
                        Collectors.summingLong(Receipt::getTotalAmount)
                ));

        Map<Long, Long> burdens = shares.stream()
                .collect(Collectors.groupingBy(
                        share -> share.getTripMember().getId(),
                        Collectors.summingLong(ItemShare::getShareAmount)
                ));

        List<SettlementResultResponse.Participant> participants =
                members.stream()
                        .map(member -> SettlementResultResponse.Participant.of(
                                member,
                                payments.getOrDefault(member.getId(), 0L),
                                burdens.getOrDefault(member.getId(), 0L)
                        ))
                        .toList();

        List<SettlementResultResponse.Transfer> transfers =
                settlementTransferRepository
                        .findAllBySettlement_IdOrderByIdAsc(settlement.getId())
                        .stream()
                        .map(SettlementResultResponse.Transfer::from)
                        .toList();

        return SettlementResultResponse.Result.of(
                settlement,
                participants,
                transfers
        );
    }


}
