package AIFinance.demo.settlement.dto;

import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;

import java.time.LocalDateTime;

public class SettlementResponse {

    private SettlementResponse() {}

    public record TransferSent(Long transferId, Long settlementId, SettlementTransferStatus status, LocalDateTime sentAt) {
        public static TransferSent from(SettlementTransfer transfer){
            return new TransferSent(
                    transfer.getId(),
                    transfer.getSettlement().getId(),
                    transfer.getStatus(),
                    transfer.getSentAt()
            );
        }
    }

    public record TransferConfirmed(Long transferId, Long settlementId, SettlementTransferStatus status, LocalDateTime sentAt, LocalDateTime confirmedAt) {
        public static TransferConfirmed from(SettlementTransfer transfer){
            return new TransferConfirmed(
                    transfer.getId(),
                    transfer.getSettlement().getId(),
                    transfer.getStatus(),
                    transfer.getSentAt(),
                    transfer.getConfirmedAt()
            );
        }
    }

    public record SettlementCompleted(Long settlementId, Long tripId, SettlementStatus status, LocalDateTime completedAt) {
        public static SettlementCompleted from(Settlement settlement){
            return new SettlementCompleted(
                    settlement.getId(),
                    settlement.getTrip().getId(),
                    settlement.getStatus(),
                    settlement.getCompletedAt()
            );
        }
    }

}
