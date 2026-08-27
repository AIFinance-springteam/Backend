package AIFinance.demo.receipt.dto;

import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.trip.entity.TripMember;

import java.time.LocalDateTime;
import java.util.List;

public class ReceiptResponse {

    private ReceiptResponse() {
    }

    public record Created(
            Long receiptId,
            Long tripId,
            String imageUrl,
            String status,
            String analysisStatus
    ) {

        public static Created from(Receipt receipt) {
            return new Created(
                    receipt.getId(),
                    receipt.getTrip().getId(),
                    receipt.getImageUrl(),
                    receipt.getStatus().name(),
                    receipt.getAnalysisStatus().name()
            );
        }
    }

    public record CreatedManual(
            Long receiptId,
            Long tripId,
            String merchantName,
            LocalDateTime paidAt,
            Long totalAmount,
            String status
    ) {

        public static CreatedManual from(Receipt receipt) {
            return new CreatedManual(
                    receipt.getId(),
                    receipt.getTrip().getId(),
                    receipt.getMerchantName(),
                    receipt.getPaidAt(),
                    receipt.getTotalAmount(),
                    receipt.getStatus().name()
            );
        }
    }

    public record Summary(
            Long receiptId,
            String merchantName,
            LocalDateTime paidAt,
            Long totalAmount,
            Long payerMemberId,
            String payerMemberName,
            String status
    ) {

        public static Summary from(Receipt receipt) {
            TripMember payer = receipt.getPayerMember();
            return new Summary(
                    receipt.getId(),
                    receipt.getMerchantName(),
                    receipt.getPaidAt(),
                    receipt.getTotalAmount(),
                    payer != null ? payer.getId() : null,
                    payer != null ? payer.getUser().getNickname() : null,
                    receipt.getStatus().name()
            );
        }
    }

    public record ItemSummary(
            Long itemId,
            String itemName,
            Integer quantity,
            Long originalAmount,
            Long settlementAmount
    ) {

        public static ItemSummary from(ReceiptItem item) {
            return new ItemSummary(
                    item.getId(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getOriginalAmount(),
                    item.getSettlementAmount()
            );
        }
    }

    public record Detail(
            Long receiptId,
            String merchantName,
            LocalDateTime paidAt,
            Long totalAmount,
            Long payerMemberId,
            String payerMemberName,
            String status,
            String analysisStatus,
            List<ItemSummary> items
    ) {

        public static Detail from(Receipt receipt) {
            TripMember payer = receipt.getPayerMember();
            List<ItemSummary> items = receipt.getItems().stream()
                    .map(ItemSummary::from)
                    .toList();

            return new Detail(
                    receipt.getId(),
                    receipt.getMerchantName(),
                    receipt.getPaidAt(),
                    receipt.getTotalAmount(),
                    payer != null ? payer.getId() : null,
                    payer != null ? payer.getUser().getNickname() : null,
                    receipt.getStatus().name(),
                    receipt.getAnalysisStatus().name(),
                    items
            );
        }
    }

    public record PayerChanged(
            Long receiptId,
            Long payerMemberId,
            String payerMemberName
    ) {

        public static PayerChanged from(Receipt receipt) {
            TripMember payer = receipt.getPayerMember();
            return new PayerChanged(
                    receipt.getId(),
                    payer != null ? payer.getId() : null,
                    payer != null ? payer.getUser().getNickname() : null
            );
        }
    }

    public record Updated(
            Long receiptId,
            String merchantName,
            LocalDateTime paidAt,
            Long totalAmount
    ) {

        public static Updated from(Receipt receipt) {
            return new Updated(
                    receipt.getId(),
                    receipt.getMerchantName(),
                    receipt.getPaidAt(),
                    receipt.getTotalAmount()
            );
        }
    }
}
