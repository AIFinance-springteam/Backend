package AIFinance.demo.report.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {
    REPORT_DATA_INCONSISTENT(
            HttpStatus.CONFLICT,
            "REPORT_DATA_INCONSISTENT",
            "완료된 정산 데이터의 금액이 일치하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
