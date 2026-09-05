package AIFinance.demo.settlement.repository;

import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementTransferRepository extends JpaRepository<SettlementTransfer, Long> {

    Optional<SettlementTransfer> findByIdAndSettlement_Trip_Id(Long transferId, Long tripId);

    boolean existsBySettlement_IdAndStatusNot(Long settlementId, SettlementTransferStatus status);

    List<SettlementTransfer> findAllBySettlement_IdOrderByIdAsc(Long settlementId);
}
