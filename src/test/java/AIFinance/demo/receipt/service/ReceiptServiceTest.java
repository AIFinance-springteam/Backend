package AIFinance.demo.receipt.service;

import AIFinance.demo.receipt.dto.ReceiptRequest;
import AIFinance.demo.receipt.dto.ReceiptResponse;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.enums.ReceiptAnalysisStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void uploadImage_성공하면_Receipt를_저장하고_응답을_반환한다() {
        Long tripId = 1L;
        Long userId = 1L;
        Trip trip = Trip.builder().id(tripId).status(TripStatus.ACTIVE).build();
        TripMember uploader = TripMember.builder().id(10L).build();
        ReceiptRequest.UploadImage request = new ReceiptRequest.UploadImage("https://image.test/receipt.png");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(uploader));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(invocation -> {
            Receipt receipt = invocation.getArgument(0);
            ReflectionTestUtils.setField(receipt, "id", 100L);
            return receipt;
        });

        ReceiptResponse.Created response = receiptService.uploadImage(userId, tripId, request);

        assertThat(response.receiptId()).isEqualTo(100L);
        assertThat(response.tripId()).isEqualTo(tripId);
        assertThat(response.imageUrl()).isEqualTo(request.imageUrl());
        assertThat(response.status()).isEqualTo(ReceiptStatus.ACTIVE.name());
        assertThat(response.analysisStatus()).isEqualTo(ReceiptAnalysisStatus.PENDING.name());
    }

    @Test
    void getReceiptDetail_삭제된_영수증이면_RECEIPT_DELETED_예외를_던진다() {
        Long tripId = 1L;
        Long userId = 1L;
        Long receiptId = 5L;
        TripMember member = TripMember.builder().id(2L).build();
        Receipt receipt = Receipt.builder().id(receiptId).status(ReceiptStatus.DELETED).build();

        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(member));
        when(receiptRepository.findByIdAndTrip_Id(receiptId, tripId)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> receiptService.getReceiptDetail(userId, tripId, receiptId))
                .isInstanceOf(ReceiptException.class)
                .extracting("code")
                .isEqualTo(ReceiptErrorCode.RECEIPT_DELETED);
    }

    @Test
    void changePayer_트립_멤버가_아닌_사람을_결제자로_지정하면_PAYER_NOT_TRIP_MEMBER_예외를_던진다() {
        Long tripId = 1L;
        Long userId = 1L;
        Long receiptId = 5L;
        Long payerMemberId = 999L;
        Trip trip = Trip.builder().id(tripId).status(TripStatus.ACTIVE).build();
        TripMember caller = TripMember.builder().id(2L).build();
        Receipt receipt = Receipt.builder().id(receiptId).status(ReceiptStatus.ACTIVE).build();
        ReceiptRequest.ChangePayer request = new ReceiptRequest.ChangePayer(payerMemberId);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(caller));
        when(receiptRepository.findByIdAndTrip_Id(receiptId, tripId)).thenReturn(Optional.of(receipt));
        when(tripMemberRepository.findByIdAndTrip_IdAndStatus(payerMemberId, tripId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.changePayer(userId, tripId, receiptId, request))
                .isInstanceOf(ReceiptException.class)
                .extracting("code")
                .isEqualTo(ReceiptErrorCode.PAYER_NOT_TRIP_MEMBER);

        assertThat(receipt.getPayerMember()).isNull();
    }

    @Test
    void deleteReceipt_성공하면_status가_DELETED로_변경된다() {
        Long tripId = 1L;
        Long userId = 1L;
        Long receiptId = 5L;
        Trip trip = Trip.builder().id(tripId).status(TripStatus.ACTIVE).build();
        TripMember caller = TripMember.builder().id(2L).build();
        Receipt receipt = Receipt.builder().id(receiptId).status(ReceiptStatus.ACTIVE).build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(caller));
        when(receiptRepository.findByIdAndTrip_Id(receiptId, tripId)).thenReturn(Optional.of(receipt));

        receiptService.deleteReceipt(userId, tripId, receiptId);

        assertThat(receipt.getStatus()).isEqualTo(ReceiptStatus.DELETED);
    }

    @Test
    void 트립_멤버가_아니면_TRIP_MEMBER_REQUIRED_예외를_던진다() {
        Long tripId = 1L;
        Long userId = 1L;

        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.getReceipts(userId, tripId))
                .isInstanceOf(ReceiptException.class)
                .extracting("code")
                .isEqualTo(ReceiptErrorCode.TRIP_MEMBER_REQUIRED);
    }
}
