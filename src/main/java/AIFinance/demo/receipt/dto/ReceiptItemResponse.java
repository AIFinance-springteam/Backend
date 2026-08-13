package AIFinance.demo.receipt.dto;

import AIFinance.demo.receipt.entity.ReceiptItem;

public class ReceiptItemResponse {

    private ReceiptItemResponse() {
    }

    public record CreatedAdditionalCost(
            Long itemId,
            Long receiptId,
            String itemName,
            Integer quantity,
            Long originalAmount,
            Long settlementAmount
    ) {

        public static CreatedAdditionalCost from(ReceiptItem item) {
            return new CreatedAdditionalCost(
                    item.getId(),
                    item.getReceipt().getId(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getOriginalAmount(),
                    item.getSettlementAmount()
            );
        }
    }
}