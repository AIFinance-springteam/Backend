package AIFinance.demo.trip.repository;

import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    // 여행방 Id, 유저 Id, 멤버 상태 확인
    boolean existsByTrip_IdAndUser_IdAndStatus(Long tripId, Long userId, TripMemberStatus status);

    // 여행에 속한 멤버 리스트
    List<TripMember> findByTrip_IdAndStatus(Long tripId, TripMemberStatus status);

    Optional<TripMember> findByTrip_IdAndUser_IdAndStatus(Long tripId, Long userId, TripMemberStatus status);

    List<TripMember> findAllByTrip_IdAndStatus(Long tripId, TripMemberStatus status);

    int countByTrip_IdAndStatus(Long tripId, TripMemberStatus status);

    List<TripMember> findAllByUser_IdAndStatus(Long userId, TripMemberStatus status);

    Optional<TripMember> findByIdAndTrip_IdAndStatus(Long tripMemberId, Long tripId, TripMemberStatus status);
}
