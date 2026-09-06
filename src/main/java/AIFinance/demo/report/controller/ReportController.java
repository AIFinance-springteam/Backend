package AIFinance.demo.report.controller;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.report.dto.ReportResponse;
import AIFinance.demo.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/report")
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public ApiResponse<ReportResponse> getReport(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long tripId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, reportService.getReport(userId, tripId));
    }
}
