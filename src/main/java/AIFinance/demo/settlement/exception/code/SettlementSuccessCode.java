package AIFinance.demo.settlement.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SettlementSuccessCode implements BaseSuccessCode {

    SETTLEMENT_CHECKED(HttpStatus.OK, "SETTLEMENT_CHECKED", "정산 마감 점검이 완료되었습니다."),

    TRANSFER_SENT(HttpStatus.OK, "TRANSFER_SENT", "송금 완료로 표시되었습니다."),

    TRANSFER_CONFIRMED(HttpStatus.OK, "TRANSFER_CONFIRMED", "입금이 확인되었습니다."),

    SETTLEMENT_COMPLETED(HttpStatus.OK, "SETTLEMENT_COMPLETED", "정산이 종료되었습니다."),

    SETTLEMENT_CONFIRMED(HttpStatus.OK, "SETTLEMENT_CONFIRMED", "정산이 확정되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
