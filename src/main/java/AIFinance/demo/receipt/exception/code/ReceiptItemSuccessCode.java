package AIFinance.demo.receipt.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptItemSuccessCode implements BaseSuccessCode {

    ADDITIONAL_COST_CREATED(
            HttpStatus.CREATED,
            "ADDITIONAL_COST_CREATED",
            "추가 비용 항목이 등록되었습니다."
    ),

    ADDITIONAL_COST_DELETED(
            HttpStatus.OK,
            "ADDITIONAL_COST_DELETED",
            "추가 비용 항목이 삭제되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}