package AIFinance.demo.settlement.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.settlement.entity.enums.SettlementTransferStatus;
import AIFinance.demo.trip.entity.TripMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(name = "settlement_transfers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementTransfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_member_id", nullable = false)
    private TripMember senderMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_member_id", nullable = false)
    private TripMember receiverMember;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SettlementTransferStatus status = SettlementTransferStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public static SettlementTransfer create(Settlement settlement, TripMember senderMember, TripMember receiverMember, Long amount) {
        return SettlementTransfer.builder()
                .settlement(settlement)
                .senderMember(senderMember)
                .receiverMember(receiverMember)
                .amount(amount)
                .status(SettlementTransferStatus.PENDING)
                .build();
    }

    public void markSent() {
        this.status = SettlementTransferStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status =  SettlementTransferStatus.COMPLETED;
        this.confirmedAt = LocalDateTime.now();
    }
}
