package AIFinance.demo.settlement.repository;

import AIFinance.demo.settlement.entity.SettlementTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementTransferRepository extends JpaRepository<SettlementTransfer, Long> {
}
