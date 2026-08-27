package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.receipt.dto.ItemParticipantsRequest;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.service.ItemParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/receipts/{receiptId}/items/{itemId}")
@RequiredArgsConstructor
public class ItemParticipantController {

    private final ItemParticipantService itemParticipantService;

    @PostMapping("/participants")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> selectParticipants(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId,
            @RequestBody ItemParticipantsRequest request
    ) {
        ItemParticipantsResponse response =
                itemParticipantService.selectParticipants(tripId, itemId, request);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @PostMapping("/participants/all")
    public ResponseEntity<ApiResponse<ItemParticipantsResponse>> selectAllParticipants(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        ItemParticipantsResponse response =
                itemParticipantService.selectAllParticipants(tripId, itemId);

        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}