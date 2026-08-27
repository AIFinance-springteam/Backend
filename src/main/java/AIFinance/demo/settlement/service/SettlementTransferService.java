package AIFinance.demo.settlement.service;

import AIFinance.demo.settlement.dto.SettlementResponse;
import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.settlement.repository.SettlementTransferRepository;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementTransferService {

    private final TripRepository tripRepository;
    private final SettlementTransferRepository settlementTransferRepository;

    @Transactional
    public SettlementResponse.TransferSent markTransferSent(Long userId, Long tripId, Long transferId) {
        Trip trip = getTrip(tripId); // 여행방 조회
        validateTripStatus(trip); // 여행방 상태 SETTLING 인지 조회

        SettlementTransfer transfer = getTransfer(transferId, tripId); // 여행방의 송금 건 조회
        validateSender(transfer, userId); // 현재 사용자가 해당 송금 건의 송금자인지 조회
        validateCanMarkSent(transfer); // 송금 상태 PENDING 인지 조회

        transfer.markSent();

        return SettlementResponse.TransferSent.from(transfer);
    }

    @Transactional
    public SettlementResponse.TransferConfirmed confirmTransfer(Long userId, Long tripId, Long transferId) {
        Trip trip = getTrip(tripId); // 여행방 조회
        validateTripStatus(trip); // 여행방 상태 SETTLING 인지 조회

        SettlementTransfer transfer = getTransfer(transferId, tripId); // 여행방의 송금 건 조회
        validateReceiver(transfer, userId); // 현재 사용자가 송금 건의 수취인인지 조회
        validateCanConfirm(transfer); // 여행방 상태 SENT 인지 조회

        transfer.confirm();

        return SettlementResponse.TransferConfirmed.from(transfer);
    }

    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND));
    }

    private SettlementTransfer getTransfer(Long transferId, Long tripId) {
        return settlementTransferRepository
                .findByIdAndSettlement_Trip_Id(transferId, tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRANSFER_NOT_FOUND));
    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.SETTLING) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }
    }

    private void validateSender(SettlementTransfer transfer, Long userId) {
        Long senderUserId = transfer.getSenderMember().getUser().getId();

        if (!Objects.equals(senderUserId, userId)) {
            throw new SettlementException(SettlementErrorCode.TRANSFER_SENDER_REQUIRED);
        }
    }

    private void validateReceiver(SettlementTransfer transfer, Long userId) {
        Long receiverUserId = transfer.getReceiverMember().getUser().getId();

        if (!Objects.equals(receiverUserId, userId)) {
            throw new SettlementException(SettlementErrorCode.TRANSFER_RECEIVER_REQUIRED);
        }
    }

    private void validateCanMarkSent(SettlementTransfer transfer) {
        if (transfer.getStatus() != SettlementTransferStatus.PENDING) {
            throw new SettlementException(SettlementErrorCode.TRANSFER_ALREADY_SENT);
        }
    }

    private void validateCanConfirm(SettlementTransfer transfer) {
        if (transfer.getStatus() == SettlementTransferStatus.PENDING) {
            throw new SettlementException(SettlementErrorCode.TRANSFER_NOT_SENT);
        }

        if (transfer.getStatus() == SettlementTransferStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.TRANSFER_ALREADY_CONFIRMED);
        }
    }
}
