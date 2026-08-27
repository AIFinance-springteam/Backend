package AIFinance.demo.settlement.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SettlementSuccessCode implements BaseSuccessCode {
    TRANSFER_SENT(HttpStatus.OK, "TRANSFER_SENT", "송금 완료로 표시되었습니다."),

    TRANSFER_CONFIRMED(HttpStatus.OK, "TRANSFER_CONFIRMED", "입금이 확인되었습니다."),

    SETTLEMENT_COMPLETED(HttpStatus.OK, "SETTLEMENT_COMPLETED", "정산이 종료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
