package AIFinance.demo.receipt.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.trip.entity.TripMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Table(name = "item_shares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_shares_item_member",
                        columnNames = {"item_id", "trip_member_id"}
                )
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemShare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_share_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ReceiptItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_member_id", nullable = false)
    private TripMember tripMember;

    @Column(name = "share_amount", nullable = false)
    private Long shareAmount;

    public static ItemShare of(ReceiptItem item, TripMember tripMember, Long shareAmount) {
        ItemShare share = new ItemShare();
        share.item = item;
        share.tripMember = tripMember;
        share.shareAmount = shareAmount;
        return share;
    }

    public void updateShareAmount(Long shareAmount) {
        this.shareAmount = shareAmount;
    }
}