package AIFinance.demo.settlement.repository;

import AIFinance.demo.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByTrip_Id(Long tripId);
    boolean existsByTrip_Id(Long tripId);
}
