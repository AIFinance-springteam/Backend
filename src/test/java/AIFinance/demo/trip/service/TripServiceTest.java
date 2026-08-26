package AIFinance.demo.trip.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.trip.dto.CreateTripRequest;
import AIFinance.demo.trip.dto.JoinTripResponse;
import AIFinance.demo.trip.dto.TripResponse;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberRole;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.exception.TripErrorCode;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import AIFinance.demo.user.entity.User;
import AIFinance.demo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    void createTrip_성공하면_Trip과_OWNER_TripMember를_저장하고_응답을_반환한다() {
        Long ownerId = 1L;
        User owner = User.builder().id(ownerId).nickname("owner").build();
        CreateTripRequest request = new CreateTripRequest(
                "제주도 여행",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 5)
        );

        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);
        when(tripRepository.findByInviteCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            ReflectionTestUtils.setField(trip, "id", 100L);
            return trip;
        });

        TripResponse response = tripService.createTrip(ownerId, request);

        assertThat(response.tripId()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.startDate()).isEqualTo(request.startDate());
        assertThat(response.endDate()).isEqualTo(request.endDate());
        assertThat(response.status()).isEqualTo(TripStatus.ACTIVE.name());
        assertThat(response.inviteCode()).hasSize(8);

        ArgumentCaptor<TripMember> memberCaptor = ArgumentCaptor.forClass(TripMember.class);
        verify(tripMemberRepository).save(memberCaptor.capture());
        TripMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRole()).isEqualTo(TripMemberRole.OWNER);
        assertThat(savedMember.getStatus()).isEqualTo(TripMemberStatus.ACTIVE);
        assertThat(savedMember.getUser()).isEqualTo(owner);
    }

    @Test
    void getTripDetail_멤버가_아니면_NOT_TRIP_MEMBER_예외를_던진다() {
        Long tripId = 1L;
        Long userId = 2L;
        Trip trip = Trip.builder()
                .id(tripId)
                .owner(User.builder().id(999L).nickname("owner").build())
                .name("여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> tripService.getTripDetail(userId, tripId))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(TripErrorCode.NOT_TRIP_MEMBER);
    }

    @Test
    void joinTrip_만료된_초대코드면_INVITE_EXPIRED_예외를_던진다() {
        Long userId = 1L;
        String inviteCode = "EXPIRED1";
        Trip trip = Trip.builder()
                .id(1L)
                .owner(User.builder().id(999L).nickname("owner").build())
                .name("여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .inviteCode(inviteCode)
                .inviteExpiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(tripRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.joinTrip(userId, inviteCode))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(TripErrorCode.INVITE_EXPIRED);

        verify(tripMemberRepository, never()).save(any());
    }

    @Test
    void joinTrip_이미_참여중인_멤버면_ALREADY_TRIP_MEMBER_예외를_던진다() {
        Long userId = 1L;
        String inviteCode = "VALID123";
        Trip trip = Trip.builder()
                .id(1L)
                .owner(User.builder().id(999L).nickname("owner").build())
                .name("여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .inviteCode(inviteCode)
                .inviteExpiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(tripRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(trip.getId(), userId, TripMemberStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> tripService.joinTrip(userId, inviteCode))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(TripErrorCode.ALREADY_TRIP_MEMBER);

        verify(tripMemberRepository, never()).save(any());
    }

    @Test
    void joinTrip_성공하면_MEMBER_TripMember를_저장하고_응답을_반환한다() {
        Long userId = 1L;
        String inviteCode = "VALID123";
        Trip trip = Trip.builder()
                .id(1L)
                .owner(User.builder().id(999L).nickname("owner").build())
                .name("여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .inviteCode(inviteCode)
                .inviteExpiresAt(LocalDateTime.now().plusDays(1))
                .build();
        User joiner = User.builder().id(userId).nickname("joiner").build();

        when(tripRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(trip));
        when(tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(trip.getId(), userId, TripMemberStatus.ACTIVE))
                .thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(joiner);

        JoinTripResponse response = tripService.joinTrip(userId, inviteCode);

        assertThat(response.tripId()).isEqualTo(trip.getId());

        ArgumentCaptor<TripMember> memberCaptor = ArgumentCaptor.forClass(TripMember.class);
        verify(tripMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(TripMemberRole.MEMBER);
    }

    @Test
    void leaveTrip_OWNER면_OWNER_CANNOT_LEAVE_예외를_던지고_상태를_변경하지_않는다() {
        Long tripId = 1L;
        Long userId = 1L;
        TripMember ownerMember = TripMember.builder()
                .id(1L)
                .role(TripMemberRole.OWNER)
                .status(TripMemberStatus.ACTIVE)
                .build();

        when(tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE))
                .thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> tripService.leaveTrip(userId, tripId))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(TripErrorCode.OWNER_CANNOT_LEAVE);

        assertThat(ownerMember.getStatus()).isEqualTo(TripMemberStatus.ACTIVE);
    }
}
