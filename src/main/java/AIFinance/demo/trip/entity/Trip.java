package AIFinance.demo.trip.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.trip.entity.enums.TripStatus;
import AIFinance.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trips")
public class Trip extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.ACTIVE;

    @Column(name = "invite_code", nullable = false, unique = true, length = 100)
    private String inviteCode;

    @Column(name = "invite_expires_at")
    private LocalDateTime inviteExpiresAt;

    public void startSettlement() {
        this.status = TripStatus.SETTLING;
    }

    public void completeSettlement() {
        this.status = TripStatus.COMPLETED;
    }


    public void regenerateInvite(String inviteCode, LocalDateTime inviteExpiresAt) {
        this.inviteCode = inviteCode;
        this.inviteExpiresAt = inviteExpiresAt;
    }
}
