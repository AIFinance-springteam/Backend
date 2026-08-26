package AIFinance.demo.trip.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.trip.dto.*;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberRole;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.trip.exception.TripErrorCode;
import AIFinance.demo.trip.repository.TripMemberRepository;
import AIFinance.demo.trip.repository.TripRepository;
import AIFinance.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final long INVITE_EXPIRATION_DAYS = 7;

    private final SecureRandom secureRandom = new SecureRandom();

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public TripResponse createTrip(Long ownerId, CreateTripRequest request) {
        Trip trip = Trip.builder()
                .owner(userRepository.getReferenceById(ownerId))
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(TripStatus.ACTIVE)
                .inviteCode(generateUniqueInviteCode())
                .inviteExpiresAt(LocalDateTime.now().plusDays(INVITE_EXPIRATION_DAYS))
                .build();
        tripRepository.save(trip);

        TripMember owner = TripMember.builder()
                .trip(trip)
                .user(userRepository.getReferenceById(ownerId))
                .role(TripMemberRole.OWNER)
                .status(TripMemberStatus.ACTIVE)
                .build();
        tripMemberRepository.save(owner);

        return new TripResponse(
                trip.getId(),
                trip.getName(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus().name(),
                trip.getInviteCode()
        );
    }

    public List<TripSummaryResponse> getMyTrips(Long userId) {
        return tripMemberRepository.findAllByUser_IdAndStatus(userId, TripMemberStatus.ACTIVE).stream()
                .map(TripMember::getTrip)
                .map(trip -> new TripSummaryResponse(
                        trip.getId(),
                        trip.getName(),
                        trip.getStartDate(),
                        trip.getEndDate(),
                        trip.getStatus().name(),
                        tripMemberRepository.countByTrip_IdAndStatus(trip.getId(), TripMemberStatus.ACTIVE)
                ))
                .toList();
    }

    public TripDetailResponse getTripDetail(Long userId, Long tripId) {
        Trip trip = getTripOrThrow(tripId);
        validateActiveMember(tripId, userId);

        List<MemberResponse> members = tripMemberRepository.findAllByTrip_IdAndStatus(tripId, TripMemberStatus.ACTIVE).stream()
                .map(this::toMemberResponse)
                .toList();

        return new TripDetailResponse(
                trip.getId(),
                trip.getName(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus().name(),
                trip.getOwner().getId(),
                trip.getOwner().getNickname(),
                members
        );
    }

    @Transactional
    public InviteCreateResponse createInvite(Long userId, Long tripId) {
        Trip trip = getTripOrThrow(tripId);
        if (!trip.getOwner().getId().equals(userId)) {
            throw new GeneralException(TripErrorCode.FORBIDDEN_INVITE_ACTION);
        }

        LocalDateTime inviteExpiresAt = LocalDateTime.now().plusDays(INVITE_EXPIRATION_DAYS);
        trip.regenerateInvite(generateUniqueInviteCode(), inviteExpiresAt);

        return new InviteCreateResponse(trip.getInviteCode(), trip.getInviteExpiresAt());
    }

    public InvitePreviewResponse previewInvite(String inviteCode) {
        Trip trip = getValidInviteOrThrow(inviteCode);
        int memberCount = tripMemberRepository.countByTrip_IdAndStatus(trip.getId(), TripMemberStatus.ACTIVE);

        return new InvitePreviewResponse(
                trip.getId(),
                trip.getName(),
                trip.getStartDate(),
                trip.getEndDate(),
                memberCount,
                trip.getOwner().getNickname()
        );
    }

    @Transactional
    public JoinTripResponse joinTrip(Long userId, String inviteCode) {
        Trip trip = getValidInviteOrThrow(inviteCode);

        if (tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(trip.getId(), userId, TripMemberStatus.ACTIVE)) {
            throw new GeneralException(TripErrorCode.ALREADY_TRIP_MEMBER);
        }

        TripMember member = TripMember.builder()
                .trip(trip)
                .user(userRepository.getReferenceById(userId))
                .role(TripMemberRole.MEMBER)
                .status(TripMemberStatus.ACTIVE)
                .build();
        tripMemberRepository.save(member);

        return new JoinTripResponse(trip.getId());
    }

    public List<MemberResponse> getMembers(Long userId, Long tripId) {
        getTripOrThrow(tripId);
        validateActiveMember(tripId, userId);

        return tripMemberRepository.findAllByTrip_IdAndStatus(tripId, TripMemberStatus.ACTIVE).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public void leaveTrip(Long userId, Long tripId) {
        TripMember member = tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new GeneralException(TripErrorCode.NOT_TRIP_MEMBER));

        if (member.getRole() == TripMemberRole.OWNER) {
            throw new GeneralException(TripErrorCode.OWNER_CANNOT_LEAVE);
        }

        member.leave(LocalDateTime.now());
    }

    private Trip getTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new GeneralException(TripErrorCode.TRIP_NOT_FOUND));
    }

    private void validateActiveMember(Long tripId, Long userId) {
        if (!tripMemberRepository.existsByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)) {
            throw new GeneralException(TripErrorCode.NOT_TRIP_MEMBER);
        }
    }

    private Trip getValidInviteOrThrow(String inviteCode) {
        Trip trip = tripRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new GeneralException(TripErrorCode.INVITE_NOT_FOUND));

        if (trip.getInviteExpiresAt() == null || trip.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new GeneralException(TripErrorCode.INVITE_EXPIRED);
        }

        return trip;
    }

    private MemberResponse toMemberResponse(TripMember member) {
        return new MemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }

    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = generateInviteCode();
        } while (tripRepository.findByInviteCode(inviteCode).isPresent());
        return inviteCode;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(secureRandom.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
