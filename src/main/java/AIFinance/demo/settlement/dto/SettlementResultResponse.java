package AIFinance.demo.settlement.dto;

import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;
import AIFinance.demo.trip.entity.TripMember;

import java.time.LocalDateTime;
import java.util.List;

public class SettlementResultResponse {
    private SettlementResultResponse() {}

    public record Result(
            Long settlementId,
            Long tripId,
            SettlementStatus status,
            Long totalAmount,
            LocalDateTime confirmedAt,
            LocalDateTime completedAt,
            List<Participant> participants,
            List<Transfer> transfers) {
        public static Result of(Settlement settlement, List<Participant> participants, List<Transfer> transfers) {
            return new Result(
                    settlement.getId(),
                    settlement.getTrip().getId(),
                    settlement.getStatus(),
                    settlement.getTotalAmount(),
                    settlement.getConfirmedAt(),
                    settlement.getCompletedAt(),
                    List.copyOf(participants),
                    List.copyOf(transfers)
            );
        }
    }

    public record Participant(Long tripMemberId, Long userId, String nickname, Long paymentAmount, Long shareAmount, Long differenceAmount){
        public static Participant of(TripMember member, Long paymentAmount, Long shareAmount) {
            return new Participant(member.getId(), member.getUser().getId(), member.getUser().getNickname(), paymentAmount, shareAmount, paymentAmount - shareAmount);
        }
    }

    public record Transfer(
            Long transferId,
            Long senderMemberId,
            String senderNickname,
            Long receiverMemberId,
            String receiverNickname,
            Long amount,
            SettlementTransferStatus status,
            LocalDateTime sentAt,
            LocalDateTime confirmedAt) {
        public static Transfer from(SettlementTransfer transfer) {
            return new Transfer(
                    transfer.getId(),
                    transfer.getSenderMember().getId(),
                    transfer.getSenderMember().getUser().getNickname(),
                    transfer.getReceiverMember().getId(),
                    transfer.getReceiverMember().getUser().getNickname(),
                    transfer.getAmount(),
                    transfer.getStatus(),
                    transfer.getSentAt(),
                    transfer.getConfirmedAt());
        }
    }
}
