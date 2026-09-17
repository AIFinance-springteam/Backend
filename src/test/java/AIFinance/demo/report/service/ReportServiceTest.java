package AIFinance.demo.report.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.report.dto.ReportResponse;
import AIFinance.demo.report.exception.ReportErrorCode;
import AIFinance.demo.report.exception.ReportException;
import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.settlement.repository.SettlementRepository;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import AIFinance.demo.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    private static final long USER_ID = 1L;
    private static final long TRIP_ID = 10L;

    @Mock
    private TripRepository tripRepository;
    @Mock
    private TripMemberRepository tripMemberRepository;
    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private ReceiptItemRepository receiptItemRepository;
    @Mock
    private ItemShareRepository itemShareRepository;
    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private ReportAnalysisProvider reportAnalysisProvider;
    @InjectMocks
    private ReportService reportService;

    @Test
    void 완료된_여행의_전체_소비_리포트를_계산한다() {
        Trip trip = completedTrip();
        TripMember member1 = member(1L, USER_ID, "유녕", trip);
        TripMember member2 = member(2L, 2L, "민수", trip);
        Receipt receipt1 = receipt(101L, trip, member1, 180_000L, LocalDateTime.of(2026, 9, 1, 12, 0));
        Receipt receipt2 = receipt(102L, trip, member2, 120_000L, LocalDateTime.of(2026, 9, 2, 12, 0));
        ReceiptItem accommodation = item(201L, receipt1, "ACCOMMODATION", 180_000L);
        ReceiptItem food = item(202L, receipt2, "FOOD", 120_000L);
        List<ItemShare> shares = List.of(
                ItemShare.of(accommodation, member1, 150_000L),
                ItemShare.of(accommodation, member2, 30_000L),
                ItemShare.of(food, member2, 120_000L)
        );

        Settlement finalSettlement = prepareReport(trip, List.of(member1, member2), List.of(receipt1, receipt2),
                List.of(accommodation, food), shares);
        when(reportAnalysisProvider.analyze(org.mockito.ArgumentMatchers.any()))
                .thenReturn("구조화된 데이터를 사용한 분석입니다.");

        ReportResponse response = reportService.getReport(USER_ID, TRIP_ID);

        assertThat(response.tripName()).isEqualTo("부산 여행");
        assertThat(response.summary().totalAmount()).isEqualTo(finalSettlement.getTotalAmount());
        assertThat(response.summary()).isEqualTo(new ReportResponse.Summary(300_000L, 150_000L, 2));
        assertThat(response.categories()).extracting(ReportResponse.CategoryExpense::category)
                .containsExactly("ACCOMMODATION", "FOOD");
        assertThat(response.categories()).extracting(ReportResponse.CategoryExpense::percentage)
                .containsExactly(60, 40);
        assertThat(response.dailyExpenses()).extracting(ReportResponse.DailyExpense::date)
                .containsExactly(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2));
        assertThat(response.memberExpenses()).containsExactly(
                new ReportResponse.MemberExpense(1L, "유녕", 180_000L, 150_000L),
                new ReportResponse.MemberExpense(2L, "민수", 120_000L, 150_000L)
        );
        assertThat(response.memberExpenses().stream().mapToLong(ReportResponse.MemberExpense::paidAmount).sum())
                .isEqualTo(response.summary().totalAmount());
        assertThat(response.memberExpenses().stream().mapToLong(ReportResponse.MemberExpense::shareAmount).sum())
                .isEqualTo(response.summary().totalAmount());
        assertThat(response.categories().stream().mapToLong(ReportResponse.CategoryExpense::amount).sum())
                .isEqualTo(response.summary().totalAmount());
        assertThat(response.dailyExpenses().stream().mapToLong(ReportResponse.DailyExpense::amount).sum())
                .isEqualTo(response.summary().totalAmount());
        assertThat(response.aiAnalysis().content()).isEqualTo("구조화된 데이터를 사용한 분석입니다.");
    }

    @Test
    void 지출이_없는_여행은_빈_목록과_0원_요약을_반환한다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        prepareReport(trip, List.of(member), List.of(), List.of(), List.of());
        when(reportAnalysisProvider.analyze(org.mockito.ArgumentMatchers.any()))
                .thenReturn("아직 집계할 수 있는 여행 소비 내역이 없습니다.");

        ReportResponse response = reportService.getReport(USER_ID, TRIP_ID);

        assertThat(response.summary()).isEqualTo(new ReportResponse.Summary(0L, 0L, 0));
        assertThat(response.categories()).isEmpty();
        assertThat(response.dailyExpenses()).isEmpty();
        assertThat(response.memberExpenses()).containsExactly(
                new ReportResponse.MemberExpense(1L, "유녕", 0L, 0L)
        );
    }

    @Test
    void 한명과_한개_카테고리도_비율과_평균을_계산한다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        Receipt receipt = receipt(101L, trip, member, 50_000L, LocalDateTime.of(2026, 9, 1, 12, 0));
        ReceiptItem item = item(201L, receipt, "FOOD", 50_000L);
        prepareReport(trip, List.of(member), List.of(receipt), List.of(item),
                List.of(ItemShare.of(item, member, 50_000L)));
        when(reportAnalysisProvider.analyze(org.mockito.ArgumentMatchers.any())).thenReturn("식비가 전체의 100%입니다.");

        ReportResponse response = reportService.getReport(USER_ID, TRIP_ID);

        assertThat(response.summary().averagePerPerson()).isEqualTo(50_000L);
        assertThat(response.categories()).containsExactly(
                new ReportResponse.CategoryExpense("FOOD", "식비", 50_000L, 100)
        );
    }

    @Test
    void 분석_provider가_실패해도_결정적_fallback과_리포트를_반환한다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        Receipt receipt = receipt(101L, trip, member, 10_000L, LocalDateTime.of(2026, 9, 1, 12, 0));
        ReceiptItem item = item(201L, receipt, null, 10_000L);
        prepareReport(trip, List.of(member), List.of(receipt), List.of(item),
                List.of(ItemShare.of(item, member, 10_000L)));
        when(reportAnalysisProvider.analyze(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("provider unavailable"));

        ReportResponse response = reportService.getReport(USER_ID, TRIP_ID);

        assertThat(response.summary().totalAmount()).isEqualTo(10_000L);
        assertThat(response.aiAnalysis().content())
                .contains("총지출은 10,000원")
                .contains("기타 항목이 전체의 100%");
    }

    @Test
    void 존재하지_않는_여행이면_TRIP_NOT_FOUND를_던진다() {
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(SettlementException.class)
                .extracting("code")
                .isEqualTo(SettlementErrorCode.TRIP_NOT_FOUND);
    }

    @Test
    void 여행_멤버가_아니면_TRIP_MEMBER_REQUIRED를_던진다() {
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(completedTrip()));
        when(tripMemberRepository.findByTrip_IdAndUser_Id(TRIP_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(SettlementException.class)
                .extracting("code")
                .isEqualTo(SettlementErrorCode.TRIP_MEMBER_REQUIRED);
    }

    @Test
    void 정산이_완료되지_않은_여행이면_INVALID_TRIP_STATUS를_던진다() {
        Trip trip = Trip.builder().id(TRIP_ID).name("부산 여행").status(TripStatus.SETTLING).build();
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_Id(TRIP_ID, USER_ID))
                .thenReturn(Optional.of(member(1L, USER_ID, "유녕", trip)));

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(SettlementException.class)
                .extracting("code")
                .isEqualTo(SettlementErrorCode.INVALID_TRIP_STATUS);
    }

    @Test
    void 완료된_여행에_Settlement가_없으면_SETTLEMENT_NOT_FOUND를_던진다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_Id(TRIP_ID, USER_ID)).thenReturn(Optional.of(member));
        when(settlementRepository.findByTrip_Id(TRIP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(SettlementException.class)
                .extracting("code")
                .isEqualTo(SettlementErrorCode.SETTLEMENT_NOT_FOUND);
    }

    @Test
    void 완료된_여행의_Settlement가_COMPLETED가_아니면_정합성_오류를_던진다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_Id(TRIP_ID, USER_ID)).thenReturn(Optional.of(member));
        when(settlementRepository.findByTrip_Id(TRIP_ID))
                .thenReturn(Optional.of(settlement(trip, member, 10_000L, SettlementStatus.CONFIRMED)));

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(ReportException.class)
                .extracting("code")
                .isEqualTo(ReportErrorCode.REPORT_DATA_INCONSISTENT);
    }

    @Test
    void Receipt_합계가_Settlement_총액과_다르면_정합성_오류를_던진다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        Receipt receipt = receipt(101L, trip, member, 9_000L, LocalDateTime.of(2026, 9, 1, 12, 0));
        ReceiptItem item = item(201L, receipt, "FOOD", 10_000L);
        prepareReport(trip, List.of(member), List.of(receipt), List.of(item),
                List.of(ItemShare.of(item, member, 10_000L)), 10_000L);

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(ReportException.class)
                .extracting("code")
                .isEqualTo(ReportErrorCode.REPORT_DATA_INCONSISTENT);
    }

    @Test
    void ItemShare_합계가_Settlement_총액과_다르면_정합성_오류를_던진다() {
        Trip trip = completedTrip();
        TripMember member = member(1L, USER_ID, "유녕", trip);
        Receipt receipt = receipt(101L, trip, member, 10_000L, LocalDateTime.of(2026, 9, 1, 12, 0));
        ReceiptItem item = item(201L, receipt, "FOOD", 10_000L);
        prepareReport(trip, List.of(member), List.of(receipt), List.of(item),
                List.of(ItemShare.of(item, member, 9_000L)), 10_000L);

        assertThatThrownBy(() -> reportService.getReport(USER_ID, TRIP_ID))
                .isInstanceOf(ReportException.class)
                .extracting("code")
                .isEqualTo(ReportErrorCode.REPORT_DATA_INCONSISTENT);
    }

    private Settlement prepareReport(
            Trip trip,
            List<TripMember> members,
            List<Receipt> receipts,
            List<ReceiptItem> items,
            List<ItemShare> shares
    ) {
        long settlementTotalAmount = receipts.stream()
                .map(Receipt::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        return prepareReport(trip, members, receipts, items, shares, settlementTotalAmount);
    }

    private Settlement prepareReport(
            Trip trip,
            List<TripMember> members,
            List<Receipt> receipts,
            List<ReceiptItem> items,
            List<ItemShare> shares,
            long settlementTotalAmount
    ) {
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.findByTrip_IdAndUser_Id(TRIP_ID, USER_ID))
                .thenReturn(Optional.of(members.get(0)));
        Settlement finalSettlement = settlement(
                trip, members.get(0), settlementTotalAmount, SettlementStatus.COMPLETED);
        when(settlementRepository.findByTrip_Id(TRIP_ID)).thenReturn(Optional.of(finalSettlement));
        when(tripMemberRepository.findAllByTrip_Id(TRIP_ID)).thenReturn(members);
        when(receiptRepository.findAllByTrip_IdAndStatus(TRIP_ID, ReceiptStatus.CONFIRMED)).thenReturn(receipts);
        if (!receipts.isEmpty()) {
            when(receiptItemRepository.findByReceipt_IdIn(receipts.stream().map(Receipt::getId).toList())).thenReturn(items);
        }
        if (!items.isEmpty()) {
            when(itemShareRepository.findByItem_IdIn(items.stream().map(ReceiptItem::getId).toList())).thenReturn(shares);
        }
        return finalSettlement;
    }

    private Trip completedTrip() {
        return Trip.builder().id(TRIP_ID).name("부산 여행").status(TripStatus.COMPLETED).build();
    }

    private TripMember member(Long memberId, Long userId, String nickname, Trip trip) {
        return TripMember.builder()
                .id(memberId)
                .trip(trip)
                .user(User.builder().id(userId).nickname(nickname).build())
                .status(TripMemberStatus.ACTIVE)
                .build();
    }

    private Settlement settlement(
            Trip trip,
            TripMember confirmedByMember,
            Long totalAmount,
            SettlementStatus status
    ) {
        return Settlement.builder()
                .id(1L)
                .trip(trip)
                .confirmedByMember(confirmedByMember)
                .totalAmount(totalAmount)
                .status(status)
                .confirmedAt(LocalDateTime.of(2026, 9, 2, 12, 0))
                .completedAt(status == SettlementStatus.COMPLETED
                        ? LocalDateTime.of(2026, 9, 3, 12, 0)
                        : null)
                .build();
    }

    private Receipt receipt(Long id, Trip trip, TripMember payer, Long totalAmount, LocalDateTime paidAt) {
        return Receipt.builder()
                .id(id)
                .trip(trip)
                .payerMember(payer)
                .totalAmount(totalAmount)
                .paidAt(paidAt)
                .status(ReceiptStatus.CONFIRMED)
                .build();
    }

    private ReceiptItem item(Long id, Receipt receipt, String category, Long amount) {
        return ReceiptItem.builder()
                .id(id)
                .receipt(receipt)
                .itemName("항목")
                .quantity(1)
                .originalAmount(amount)
                .settlementAmount(amount)
                .category(category)
                .build();
    }
}
