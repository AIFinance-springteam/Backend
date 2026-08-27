package AIFinance.demo.settlement.service;

import AIFinance.demo.settlement.dto.SettlementResponse;
import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.settlement.repository.SettlementRepository;
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
public class SettlementService {

    private final TripRepository tripRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementTransferRepository settlementTransferRepository;

    @Transactional
    public SettlementResponse.SettlementCompleted completeSettlement(Long userId, Long tripId) {
        Trip trip = getTrip(tripId);
        validateOwner(trip, userId);

        Settlement settlement = getSettlement(tripId);

        validateSettlementStatus(settlement);
        validateTripStatus(trip);
        validateAllTransfersCompleted(settlement);

        settlement.complete();
        trip.completeSettlement();

        return SettlementResponse.SettlementCompleted.from(settlement);
    }

    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.TRIP_NOT_FOUND));
    }

    private Settlement getSettlement(Long tripId) {
        return settlementRepository.findByTrip_Id(tripId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));
    }

    private void validateOwner(Trip trip, Long userId) {
        Long ownerUserId = trip.getOwner().getId();

        if (!Objects.equals(userId, ownerUserId)) {
            throw new SettlementException(SettlementErrorCode.TRIP_OWNER_REQUIRED);
        }
    }

    private void validateSettlementStatus(Settlement settlement) {
        if (settlement.getStatus() == SettlementStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.SETTLEMENT_ALREADY_COMPLETED);
        }
    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.SETTLING) {
            throw new SettlementException(SettlementErrorCode.INVALID_TRIP_STATUS);
        }
    }

    private void validateAllTransfersCompleted(Settlement settlement) {

        boolean hasIncompleteTransfers = settlementTransferRepository
                .existsBySettlement_IdAndStatusNot(settlement.getId(), SettlementTransferStatus.COMPLETED);

        if (hasIncompleteTransfers) {
            throw new SettlementException(SettlementErrorCode.INCOMPLETE_TRANSFERS);
        }
    }
}
