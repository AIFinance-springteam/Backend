package AIFinance.demo.trip.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.trip.dto.DashboardAmountResponse;
import AIFinance.demo.trip.dto.DashboardDifferenceResponse;
import AIFinance.demo.trip.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/total-expense")
    public ResponseEntity<ApiResponse<DashboardAmountResponse>> getTotalExpense(
            @PathVariable Long tripId
    ) {
        DashboardAmountResponse response = dashboardService.getTotalExpense(tripId);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
    @GetMapping("/my-payments")
    public ResponseEntity<ApiResponse<DashboardAmountResponse>> getMyPayments(
            @PathVariable Long tripId
    ) {
        DashboardAmountResponse response = dashboardService.getMyPayments(tripId);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @GetMapping("/my-shares")
    public ResponseEntity<ApiResponse<DashboardAmountResponse>> getMyShares(
            @PathVariable Long tripId
    ) {
        DashboardAmountResponse response = dashboardService.getMyShares(tripId);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @GetMapping("/expected-difference")
    public ResponseEntity<ApiResponse<DashboardDifferenceResponse>> getExpectedDifference(
            @PathVariable Long tripId
    ) {
        DashboardDifferenceResponse response = dashboardService.getExpectedDifference(tripId);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}