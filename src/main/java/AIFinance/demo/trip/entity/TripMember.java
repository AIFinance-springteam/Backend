package AIFinance.demo.trip.entity;

import AIFinance.demo.trip.entity.enums.TripMemberRole;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "trip_members",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_trip_members_trip_user",
                    columnNames = {"trip_id", "user_id"}
            )
        })
public class TripMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private TripMemberRole role = TripMemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TripMemberStatus status = TripMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public void leave(LocalDateTime leftAt) {
        this.status = TripMemberStatus.LEFT;
        this.leftAt = leftAt;
    }
}
