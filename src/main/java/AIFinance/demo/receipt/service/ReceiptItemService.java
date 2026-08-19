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
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        validateTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        ReceiptItem receiptItem = ReceiptItem.createAdditionalCost(receipt, request.itemName(), request.amount());

        ReceiptItem savedItem = receiptItemRepository.save(receiptItem);

        return ReceiptItemResponse.CreatedAdditionalCost.from(savedItem);
    }

    @Transactional
    public void deleteAdditionalCost(Long userId, Long tripId, Long receiptId, Long itemId) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        validateTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        ReceiptItem receiptItem = receiptItemRepository.findByIdAndReceipt_Id(itemId, receiptId)
                        .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.RECEIPT_ITEM_NOT_FOUND));

        validateAdditionalCost(receiptItem);

        receiptItemRepository.delete(receiptItem);
    }

    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.TRIP_NOT_FOUND));
    }

    private Receipt getReceipt(Long receiptId, Long tripId) {
        return receiptRepository.findByIdAndTrip_Id(receiptId, tripId)
                .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.RECEIPT_NOT_FOUND));
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

    private void validateAdditionalCost(ReceiptItem receiptItem) {
        if (!receiptItem.isAdditionalCost()) {
            throw new ReceiptItemException(
                    ReceiptItemErrorCode.NOT_ADDITIONAL_COST
            );
        }
    }
}