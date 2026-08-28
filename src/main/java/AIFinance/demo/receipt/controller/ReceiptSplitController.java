package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.receipt.dto.UnassignedItemsResponse;
import AIFinance.demo.receipt.service.ItemSplitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/receipts/{receiptId}")
@RequiredArgsConstructor
public class ReceiptSplitController {

    private final ItemSplitService itemSplitService;

    @GetMapping("/unassigned-items")
    public ResponseEntity<ApiResponse<UnassignedItemsResponse>> getUnassignedItems(
            @PathVariable Long tripId,
            @PathVariable Long receiptId
    ) {
        UnassignedItemsResponse response = itemSplitService.getUnassignedItems(receiptId);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}