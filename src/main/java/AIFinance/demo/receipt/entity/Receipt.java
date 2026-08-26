package AIFinance.demo.receipt.entity;

import AIFinance.demo.global.entity.BaseEntity;
import AIFinance.demo.receipt.entity.enums.ReceiptAnalysisStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptDuplicateStatus;
import AIFinance.demo.receipt.entity.enums.ReceiptInputType;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import AIFinance.demo.trip.entity.Trip;
import AIFinance.demo.trip.entity.TripMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Table(name = "receipts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Receipt extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_member_id", nullable = false)
    private TripMember uploaderMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_member_id")
    private TripMember payerMember;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "merchant_name", length = 150)
    private String merchantName;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 20)
    @Builder.Default
    private ReceiptInputType inputType = ReceiptInputType.AI;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReceiptStatus status = ReceiptStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 20)
    @Builder.Default
    private ReceiptAnalysisStatus analysisStatus = ReceiptAnalysisStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "duplicate_status", nullable = false, length = 20)
    @Builder.Default
    private ReceiptDuplicateStatus duplicateStatus = ReceiptDuplicateStatus.PENDING;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceiptItem> items = new ArrayList<>();

    public void addItem(ReceiptItem item) {
        items.add(item);
        item.assignReceipt(this);
    }
}
