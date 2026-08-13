package AIFinance.demo.receipt.service;

import AIFinance.demo.receipt.dto.ReceiptItemRequest;
import AIFinance.demo.receipt.dto.ReceiptItemResponse;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.exception.ReceiptItemException;
import AIFinance.demo.receipt.exception.code.ReceiptItemErrorCode;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptItemService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;

    @Transactional
    public ReceiptItemResponse.CreatedAdditionalCost createAdditionalCost(Long userId, Long tripId, Long receiptId, ReceiptItemRequest.CreateAdditionalCost request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.TRIP_NOT_FOUND));

        validateTripStatus(trip);
        validateTripMember(tripId, userId);

        Receipt receipt = receiptRepository.findByIdAndTrip_Id(receiptId, tripId)
                .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.RECEIPT_NOT_FOUND));

        validateReceiptStatus(receipt);

        ReceiptItem receiptItem = ReceiptItem.createAdditionalCost(receipt, request.itemName(), request.amount());
        ReceiptItem savedItem = receiptItemRepository.save(receiptItem);

        return ReceiptItemResponse.CreatedAdditionalCost.from(savedItem);

    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new ReceiptItemException(ReceiptItemErrorCode.INVALID_TRIP_STATUS);
        }
    }

    private void validateTripMember(Long tripId, Long userId) {
        boolean isActiveMember = tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE);

        if (!isActiveMember) {
            throw new ReceiptItemException(ReceiptItemErrorCode.TRIP_MEMBER_REQUIRED);
        }
    }

    private void validateReceiptStatus(Receipt receipt) {
        if (receipt.getStatus() == ReceiptStatus.DELETED) {
            throw new ReceiptItemException(ReceiptItemErrorCode.RECEIPT_DELETED);
        }
    }
}
