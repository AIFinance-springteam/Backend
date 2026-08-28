package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.receipt.dto.ItemIndividualRequest;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.dto.ItemRemainderRequest;
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

    @PostMapping("/remainder")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> splitRemainder(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemRemainderRequest request
    ) {
        ItemParticipantsResponse response = itemSplitService.splitRemainder(itemId, request);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @PostMapping("/individual")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> splitIndividual(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemIndividualRequest request
    ) {
        ItemParticipantsResponse response = itemSplitService.splitIndividual(tripId, itemId, request);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @PutMapping("/custom")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> splitCustom(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemCustomRequest request
    ) {
        ItemParticipantsResponse response = itemSplitService.splitCustom(tripId, itemId, request);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}