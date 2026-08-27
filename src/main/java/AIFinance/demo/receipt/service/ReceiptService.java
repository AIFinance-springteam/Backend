package AIFinance.demo.receipt.service;

import AIFinance.demo.receipt.dto.ReceiptRequest;
import AIFinance.demo.receipt.dto.ReceiptResponse;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.enums.ReceiptAnalysisStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptDuplicateStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptInputType;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.exception.ReceiptException;
import AIFinance.demo.receipt.exception.code.ReceiptErrorCode;
import AIFinance.demo.receipt.repository.ReceiptRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;

    @Transactional
    public ReceiptResponse.Created uploadImage(Long userId, Long tripId, ReceiptRequest.UploadImage request) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        TripMember uploader = getTripMember(tripId, userId);

        Receipt receipt = Receipt.builder()
                .trip(trip)
                .uploaderMember(uploader)
                .imageUrl(request.imageUrl())
                .inputType(ReceiptInputType.AI)
                .status(ReceiptStatus.ACTIVE)
                .analysisStatus(ReceiptAnalysisStatus.PENDING)
                .duplicateStatus(ReceiptDuplicateStatus.PENDING)
                .build();

        Receipt savedReceipt = receiptRepository.save(receipt);

        return ReceiptResponse.Created.from(savedReceipt);
    }

    @Transactional
    public ReceiptResponse.CreatedManual createManual(Long userId, Long tripId, ReceiptRequest.CreateManual request) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        TripMember uploader = getTripMember(tripId, userId);

        Receipt receipt = Receipt.builder()
                .trip(trip)
                .uploaderMember(uploader)
                .merchantName(request.merchantName())
                .paidAt(request.paidAt())
                .totalAmount(request.totalAmount())
                .inputType(ReceiptInputType.MANUAL)
                .status(ReceiptStatus.ACTIVE)
                .analysisStatus(ReceiptAnalysisStatus.SUCCESS)
                .duplicateStatus(ReceiptDuplicateStatus.PENDING)
                .build();

        Receipt savedReceipt = receiptRepository.save(receipt);

        return ReceiptResponse.CreatedManual.from(savedReceipt);
    }

    public List<ReceiptResponse.Summary> getReceipts(Long userId, Long tripId) {
        getTripMember(tripId, userId);

        return receiptRepository.findAllByTrip_IdAndStatusNot(tripId, ReceiptStatus.DELETED).stream()
                .map(ReceiptResponse.Summary::from)
                .toList();
    }

    public ReceiptResponse.Detail getReceiptDetail(Long userId, Long tripId, Long receiptId) {
        getTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        return ReceiptResponse.Detail.from(receipt);
    }

    @Transactional
    public ReceiptResponse.PayerChanged changePayer(Long userId, Long tripId, Long receiptId, ReceiptRequest.ChangePayer request) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        getTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        TripMember payerMember = tripMemberRepository.findByIdAndTrip_IdAndStatus(
                        request.payerMemberId(), tripId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new ReceiptException(ReceiptErrorCode.PAYER_NOT_TRIP_MEMBER));

        receipt.changePayer(payerMember);

        return ReceiptResponse.PayerChanged.from(receipt);
    }

    @Transactional
    public ReceiptResponse.Updated updateInfo(Long userId, Long tripId, Long receiptId, ReceiptRequest.UpdateInfo request) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        getTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        receipt.updateInfo(request.merchantName(), request.paidAt(), request.totalAmount());

        return ReceiptResponse.Updated.from(receipt);
    }

    @Transactional
    public void deleteReceipt(Long userId, Long tripId, Long receiptId) {
        Trip trip = getTrip(tripId);

        validateTripStatus(trip);
        getTripMember(tripId, userId);

        Receipt receipt = getReceipt(receiptId, tripId);

        validateReceiptStatus(receipt);

        receipt.delete();
    }

    private Trip getTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ReceiptException(ReceiptErrorCode.TRIP_NOT_FOUND));
    }

    private Receipt getReceipt(Long receiptId, Long tripId) {
        return receiptRepository.findByIdAndTrip_Id(receiptId, tripId)
                .orElseThrow(() -> new ReceiptException(ReceiptErrorCode.RECEIPT_NOT_FOUND));
    }

    private void validateTripStatus(Trip trip) {
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new ReceiptException(ReceiptErrorCode.INVALID_TRIP_STATUS);
        }
    }

    private TripMember getTripMember(Long tripId, Long userId) {
        return tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new ReceiptException(ReceiptErrorCode.TRIP_MEMBER_REQUIRED));
    }

    private void validateReceiptStatus(Receipt receipt) {
        if (receipt.getStatus() == ReceiptStatus.DELETED) {
            throw new ReceiptException(ReceiptErrorCode.RECEIPT_DELETED);
        }
    }
}
