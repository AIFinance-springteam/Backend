package AIFinance.demo.trip.repository;

import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    // 여행방 Id, 유저 Id, 멤버 상태 확인
    boolean existsByTrip_IdAndUser_IdAndStatus(Long tripId, Long userId, TripMemberStatus status);

    // 여행에 속한 멤버 리스트
    List<TripMember> findByTrip_IdAndStatus(Long tripId, TripMemberStatus status);
}
