package AIFinance.demo.receipt.controller;
import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.global.security.SecurityUtil;
import AIFinance.demo.receipt.dto.ReceiptAnalysisResponse;
import AIFinance.demo.receipt.service.ReceiptAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/trips/{tripId}/receipts")
public class ReceiptAnalysisController {
    private final ReceiptAnalysisService service;
    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReceiptAnalysisResponse>> analyze(
            @PathVariable Long tripId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(
                GeneralSuccessCode.CREATED,
                service.analyze(SecurityUtil.getCurrentUserId(), tripId, file)
        ));
    }
}
