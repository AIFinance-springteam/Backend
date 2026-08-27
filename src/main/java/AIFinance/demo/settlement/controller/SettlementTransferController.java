package AIFinance.demo.settlement.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.settlement.dto.SettlementResponse;
import AIFinance.demo.settlement.exception.code.SettlementSuccessCode;
import AIFinance.demo.settlement.service.SettlementTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/transfers")
public class SettlementTransferController {

    private final SettlementTransferService settlementTransferService;

    @PatchMapping("/{transferId}/sent")
    public ApiResponse<SettlementResponse.TransferSent> markTransferSent(@AuthenticationPrincipal Long userId, @PathVariable Long tripId, @PathVariable Long transferId) {
        SettlementResponse.TransferSent response = settlementTransferService.markTransferSent(userId, tripId, transferId);

        return ApiResponse.onSuccess(SettlementSuccessCode.TRANSFER_SENT, response);
    }

    @PatchMapping("/{transferId}/confirmed")
    public ApiResponse<SettlementResponse.TransferConfirmed> confirmTransfer(@AuthenticationPrincipal Long userId, @PathVariable Long tripId, @PathVariable Long transferId) {
        SettlementResponse.TransferConfirmed response = settlementTransferService.confirmTransfer(userId, tripId, transferId);

        return ApiResponse.onSuccess(SettlementSuccessCode.TRANSFER_CONFIRMED, response);
    }

}
