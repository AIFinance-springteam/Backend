package AIFinance.demo.receipt.dto;

import AIFinance.demo.receipt.entity.Receipt;
import java.util.List;

public record ReceiptAnalysisResponse(Long receiptId, String analysisStatus, String merchantName,
                                      java.time.LocalDateTime paidAt, Long totalAmount, List<Item> items) {
    public record Item(String itemName, Integer quantity, Long unitPrice, Long originalAmount) {}
    public static ReceiptAnalysisResponse from(Receipt r) {
        return new ReceiptAnalysisResponse(r.getId(), r.getAnalysisStatus().name(), r.getMerchantName(), r.getPaidAt(), r.getTotalAmount(),
                r.getItems().stream().map(i -> new Item(i.getItemName(), i.getQuantity(), i.getUnitPrice(), i.getOriginalAmount())).toList());
    }
}
