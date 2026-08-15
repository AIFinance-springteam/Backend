package AIFinance.demo.global.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SplitErrorCode implements BaseErrorCode {

    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST,
            "SPLIT_AMOUNT_MISMATCH",
            "입력한 금액의 합이 상품 금액과 일치하지 않습니다."),

    NO_PARTICIPANT_SELECTED(HttpStatus.BAD_REQUEST,
            "SPLIT_NO_PARTICIPANT",
            "선택된 참여자가 없습니다."),

    MEMBER_NOT_IN_TRIP(HttpStatus.BAD_REQUEST,
            "SPLIT_MEMBER_NOT_IN_TRIP",
            "해당 여행의 참여 멤버가 아닙니다."),

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "SPLIT_ITEM_NOT_FOUND",
            "해당 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}