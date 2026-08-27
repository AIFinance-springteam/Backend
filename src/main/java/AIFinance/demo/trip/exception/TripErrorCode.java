package AIFinance.demo.trip.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TripErrorCode implements BaseErrorCode {
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND,
            "TRIP_NOT_FOUND",
            "여행방을 찾을 수 없습니다."),
    NOT_TRIP_MEMBER(HttpStatus.FORBIDDEN,
            "TRIP_NOT_MEMBER",
            "이 여행방의 참여자가 아닙니다."),
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "TRIP_INVITE_NOT_FOUND",
            "유효하지 않은 초대 코드입니다."),
    INVITE_EXPIRED(HttpStatus.GONE,
            "TRIP_INVITE_EXPIRED",
            "초대 링크가 만료되었습니다."),
    ALREADY_TRIP_MEMBER(HttpStatus.CONFLICT,
            "TRIP_ALREADY_MEMBER",
            "이미 참여 중인 여행방입니다."),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST,
            "TRIP_OWNER_CANNOT_LEAVE",
            "방장은 여행방을 나갈 수 없습니다."),
    FORBIDDEN_INVITE_ACTION(HttpStatus.FORBIDDEN,
            "TRIP_FORBIDDEN_INVITE",
            "방장만 초대 링크를 생성할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
