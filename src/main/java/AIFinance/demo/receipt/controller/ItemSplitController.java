package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.service.ItemSplitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/receipts/{receiptId}/items/{itemId}/split")
@RequiredArgsConstructor
public class ItemSplitController {

    private final ItemSplitService itemSplitService;

    @PostMapping("/equal")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> splitEqual(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        ItemParticipantsResponse response = itemSplitService.splitEqual(itemId);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}