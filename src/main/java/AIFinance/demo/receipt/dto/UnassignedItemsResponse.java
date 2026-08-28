package AIFinance.demo.receipt.dto;

import AIFinance.demo.receipt.entity.ReceiptItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UnassignedItemsResponse {

    private Long receiptId;
    private List<UnassignedItem> items;

    public static UnassignedItemsResponse of(Long receiptId, List<ReceiptItem> items) {
        List<UnassignedItem> unassignedItems = items.stream()
                .map(item -> UnassignedItem.builder()
                        .itemId(item.getId())
                        .itemName(item.getItemName())
                        .originalAmount(item.getOriginalAmount())
                        .build())
                .toList();

        return UnassignedItemsResponse.builder()
                .receiptId(receiptId)
                .items(unassignedItems)
                .build();
    }

    @Getter
    @Builder
    public static class UnassignedItem {
        private Long itemId;
        private String itemName;
        private Long originalAmount;
    }
}