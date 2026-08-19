package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.receipt.dto.ReceiptItemRequest;
import AIFinance.demo.receipt.dto.ReceiptItemResponse;
import AIFinance.demo.receipt.exception.code.ReceiptItemSuccessCode;
import AIFinance.demo.receipt.service.ReceiptItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.security.core.annotation.AuthenticationPrincipal;
=======
>>>>>>> cc264a4 (feat: 추가 비용 항목 삭제 API 구현)
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/receipts/{receiptId}/items")
public class ReceiptItemController {

    private final ReceiptItemService receiptItemService;

    @PostMapping
    public ApiResponse<ReceiptItemResponse.CreatedAdditionalCost>
    createAdditionalCost(
<<<<<<< HEAD
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId, @PathVariable 
            Long receiptId,
            @Valid @RequestBody ReceiptItemRequest.CreateAdditionalCost request
    ) {

        ReceiptItemResponse.CreatedAdditionalCost result = receiptItemService.createAdditionalCost(userId, tripId, receiptId, request);

        return ApiResponse.onSuccess(ReceiptItemSuccessCode.ADDITIONAL_COST_CREATED, result);
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteAdditionalCost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        receiptItemService.deleteAdditionalCost(userId, tripId, receiptId, itemId);

        return ApiResponse.onSuccess(ReceiptItemSuccessCode.ADDITIONAL_COST_DELETED, null);
=======
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody
            ReceiptItemRequest.CreateAdditionalCost request
    ) {
        ReceiptItemResponse.CreatedAdditionalCost result =
                receiptItemService.createAdditionalCost(
                        userId,
                        tripId,
                        receiptId,
                        request
                );

        return ApiResponse.onSuccess(
                ReceiptItemSuccessCode.ADDITIONAL_COST_CREATED,
                result
        );
>>>>>>> cc264a4 (feat: 추가 비용 항목 삭제 API 구현)
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteAdditionalCost(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @PathVariable Long itemId
    ) {
        receiptItemService.deleteAdditionalCost(
                userId,
                tripId,
                receiptId,
                itemId
        );

        return ApiResponse.onSuccess(
                ReceiptItemSuccessCode.ADDITIONAL_COST_DELETED,
                null
        );
    }
}