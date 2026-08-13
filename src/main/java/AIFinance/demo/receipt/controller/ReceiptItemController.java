package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.receipt.dto.ReceiptItemRequest;
import AIFinance.demo.receipt.dto.ReceiptItemResponse;
import AIFinance.demo.receipt.exception.code.ReceiptItemSuccessCode;
import AIFinance.demo.receipt.service.ReceiptItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/receipts/{receiptId}/items")
public class ReceiptItemController {
    private final ReceiptItemService receiptItemService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReceiptItemResponse.CreatedAdditionalCost>> createAdditionalCost(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody ReceiptItemRequest.CreateAdditionalCost request
    ){
        ReceiptItemResponse.CreatedAdditionalCost result = receiptItemService.createAdditionalCost(userId, tripId, receiptId, request);
        ReceiptItemSuccessCode successCode = ReceiptItemSuccessCode.ADDITIONAL_COST_CREATED;

        return ResponseEntity
                .status(successCode.getStatus())
                .body(ApiResponse.onSuccess(successCode, result));
    }
}
