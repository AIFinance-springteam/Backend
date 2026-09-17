package AIFinance.demo.receipt.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.global.security.SecurityUtil;
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
            @PathVariable Long tripId,
            @Valid @RequestBody ReceiptRequest.UploadImage request
    ) {
        ReceiptResponse.Created result = receiptService.uploadImage(SecurityUtil.getCurrentUserId(), tripId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_UPLOADED, result);
    }

    @PostMapping("/manual")
    public ApiResponse<ReceiptResponse.CreatedManual> createManual(
            @PathVariable Long tripId,
            @Valid @RequestBody ReceiptRequest.CreateManual request
    ) {
        ReceiptResponse.CreatedManual result = receiptService.createManual(SecurityUtil.getCurrentUserId(), tripId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_MANUAL_CREATED, result);
    }

    @GetMapping
    public ApiResponse<List<ReceiptResponse.Summary>> getReceipts(
            @PathVariable Long tripId
    ) {
        List<ReceiptResponse.Summary> result = receiptService.getReceipts(SecurityUtil.getCurrentUserId(), tripId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @GetMapping("/{receiptId}")
    public ApiResponse<ReceiptResponse.Detail> getReceiptDetail(
            @PathVariable Long tripId,
            @PathVariable Long receiptId
    ) {
        ReceiptResponse.Detail result = receiptService.getReceiptDetail(SecurityUtil.getCurrentUserId(), tripId, receiptId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @PatchMapping("/{receiptId}/payer")
    public ApiResponse<ReceiptResponse.PayerChanged> changePayer(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody ReceiptRequest.ChangePayer request
    ) {
        ReceiptResponse.PayerChanged result = receiptService.changePayer(SecurityUtil.getCurrentUserId(), tripId, receiptId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_PAYER_CHANGED, result);
    }

    @PatchMapping("/{receiptId}")
    public ApiResponse<ReceiptResponse.Updated> updateInfo(
            @PathVariable Long tripId,
            @PathVariable Long receiptId,
            @Valid @RequestBody ReceiptRequest.UpdateInfo request
    ) {
        ReceiptResponse.Updated result = receiptService.updateInfo(SecurityUtil.getCurrentUserId(), tripId, receiptId, request);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_UPDATED, result);
    }

    @DeleteMapping("/{receiptId}")
    public ApiResponse<Void> deleteReceipt(
            @PathVariable Long tripId,
            @PathVariable Long receiptId
    ) {
        receiptService.deleteReceipt(SecurityUtil.getCurrentUserId(), tripId, receiptId);

        return ApiResponse.onSuccess(ReceiptSuccessCode.RECEIPT_DELETED, null);
    }
}
