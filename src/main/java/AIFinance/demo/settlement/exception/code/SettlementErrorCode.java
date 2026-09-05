package AIFinance.demo.settlement.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SettlementErrorCode implements BaseErrorCode {

    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP_NOT_FOUND", "여행방을 찾을 수 없습니다."),

    INVALID_TRIP_STATUS(HttpStatus.CONFLICT, "INVALID_TRIP_STATUS", "현재 여행 상태에서는 요청을 처리할 수 없습니다."),

    TRANSFER_NOT_FOUND(HttpStatus.NOT_FOUND, "TRANSFER_NOT_FOUND", "해당 여행방에서 송금 건을 찾을 수 없습니다."),

    TRANSFER_SENDER_REQUIRED(HttpStatus.FORBIDDEN, "TRANSFER_SENDER_REQUIRED", "해당 송금 건의 송금자만 완료 표시할 수 있습니다."),

    TRANSFER_RECEIVER_REQUIRED(HttpStatus.FORBIDDEN, "TRANSFER_RECEIVER_REQUIRED", "해당 송금 건의 수취인만 입금을 확인할 수 있습니다."),

    TRANSFER_ALREADY_SENT(HttpStatus.CONFLICT, "TRANSFER_ALREADY_SENT", "이미 송금 완료로 표시된 송금 건입니다."),

    TRANSFER_NOT_SENT(HttpStatus.CONFLICT, "TRANSFER_NOT_SENT", "송금 완료 표시 후 입금을 확인할 수 있습니다."),

    TRANSFER_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "TRANSFER_ALREADY_CONFIRMED", "이미 입금 확인이 완료된 송금 건입니다."),

    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_NOT_FOUND", "확정된 정산을 찾을 수 없습니다."),

    TRIP_OWNER_REQUIRED(HttpStatus.FORBIDDEN, "TRIP_OWNER_REQUIRED", "여행방의 방장만 요청할 수 있습니다."),

    TRIP_MEMBER_REQUIRED(HttpStatus.FORBIDDEN, "TRIP_MEMBER_REQUIRED", "해당 여행방의 참여자만 요청할 수 있습니다."),

    SETTLEMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SETTLEMENT_ALREADY_COMPLETED", "이미 종료된 정산입니다."),

    INCOMPLETE_TRANSFERS(HttpStatus.CONFLICT, "INCOMPLETE_TRANSFERS", "완료되지 않은 송금 건이 남아 있습니다."),

    SETTLEMENT_NOT_READY(HttpStatus.CONFLICT, "SETTLEMENT_NOT_READY", "미완료 항목이 남아 있어 정산을 확정할 수 없습니다."),

    SETTLEMENT_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "SETTLEMENT_ALREADY_CONFIRMED", "이미 확정된 정산입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
