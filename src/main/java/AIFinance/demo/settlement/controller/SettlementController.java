package AIFinance.demo.settlement.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.settlement.dto.SettlementResponse;
import AIFinance.demo.settlement.exception.code.SettlementSuccessCode;
import AIFinance.demo.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    @PatchMapping("/complete")
    public ApiResponse<SettlementResponse.SettlementCompleted> completeSettlement(@AuthenticationPrincipal Long userId, @PathVariable Long tripId) {
        SettlementResponse.SettlementCompleted response = settlementService.completeSettlement(userId, tripId);

        return ApiResponse.onSuccess(SettlementSuccessCode.SETTLEMENT_COMPLETED, response);
    }
}
