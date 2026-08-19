package AIFinance.demo.receipt.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.receipt.entity.enums.SplitType;
import AIFinance.demo.trip.entity.TripMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Table(name = "receipt_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReceiptItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "original_amount", nullable = false)
    private Long originalAmount;

    @Column(name = "settlement_amount", nullable = false)
    private Long settlementAmount;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", length = 20)
    private SplitType splitType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remainder_member_id")
    private TripMember remainderMember;

    void assignReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public static ReceiptItem createAdditionalCost(
            Receipt receipt,
            String itemName,
            Long amount
    ) {
        ReceiptItem item = new ReceiptItem();
        item.itemName = itemName;
        item.quantity = 1;
        item.unitPrice = amount;
        item.originalAmount = amount;
        item.settlementAmount = amount;
        item.category = "ADDITIONAL_COST";
        receipt.addItem(item);

        return item;
    }
}
