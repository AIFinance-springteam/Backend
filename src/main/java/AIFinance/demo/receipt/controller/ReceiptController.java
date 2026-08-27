package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.receipt.dto.ReceiptRequest;
import AIFinance.demo.receipt.dto.ReceiptResponse;
import AIFinance.demo.receipt.exception.code.ReceiptSuccessCode;
import AIFinance.demo.receipt.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ApiResponse<ReceiptResponse.Created> uploadImage(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody ReceiptRequest.UploadImage request
    ) {
        ReceiptResponse.Created result = receiptService.uploadImage(userId, tripId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_UPLOADED, result);
    }

    @PostMapping("/manual")
    public ApiResponse<ReceiptResponse.CreatedManual> createManual(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @Valid @RequestBody ReceiptRequest.CreateManual request
    ) {
        ReceiptResponse.CreatedManual result = receiptService.createManual(userId, tripId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_MANUAL_CREATED, result);
    }

    @GetMapping
    public ApiResponse<List<ReceiptResponse.Summary>> getReceipts(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId
    ) {
        List<ReceiptResponse.Summary> result = receiptService.getReceipts(userId, tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/{receiptId}")
    public ApiResponse<ReceiptResponse.Detail> getReceiptDetail(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId
    ) {
        ReceiptResponse.Detail result = receiptService.getReceiptDetail(userId, tripId, receiptId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @PatchMapping("/{receiptId}/payer")
    public ApiResponse<ReceiptResponse.PayerChanged> changePayer(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody ReceiptRequest.ChangePayer request
    ) {
        ReceiptResponse.PayerChanged result = receiptService.changePayer(userId, tripId, receiptId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_PAYER_CHANGED, result);
    }

    @PatchMapping("/{receiptId}")
    public ApiResponse<ReceiptResponse.Updated> updateInfo(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody ReceiptRequest.UpdateInfo request
    ) {
        ReceiptResponse.Updated result = receiptService.updateInfo(userId, tripId, receiptId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_UPDATED, result);
    }

    @DeleteMapping("/{receiptId}")
    public ApiResponse<Void> deleteReceipt(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long receiptId
    ) {
        receiptService.deleteReceipt(userId, tripId, receiptId);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_DELETED, null);
    }
}
