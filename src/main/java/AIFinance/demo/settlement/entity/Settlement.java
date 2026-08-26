package AIFinance.demo.settlement.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.settlement.entity.enums.SettlementStatus;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlements_trip",
                        columnNames = "trip_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "confirmed_by_member_id", nullable = false)
    private TripMember confirmedByMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.CONFIRMED;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static Settlement create(Trip trip, TripMember confirmedByMember, Long totalAmount) {
        return Settlement.builder()
                .trip(trip)
                .confirmedByMember(confirmedByMember)
                .totalAmount(totalAmount)
                .status(SettlementStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now())
                .build();
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

}
