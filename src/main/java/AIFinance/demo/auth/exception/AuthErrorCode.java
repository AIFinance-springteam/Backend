package AIFinance.demo.auth.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    EMAIL_DUPLICATE(HttpStatus.CONFLICT,
            "AUTH_EMAIL_DUPLICATE",
            "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATE(HttpStatus.CONFLICT,
            "AUTH_NICKNAME_DUPLICATE",
            "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_REFRESH_TOKEN",
            "유효하지 않은 리프레시 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH_USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
