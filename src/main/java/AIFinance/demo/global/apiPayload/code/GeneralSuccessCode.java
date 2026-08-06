package AIFinance.demo.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode{
    OK(HttpStatus.OK,
            "COMMON_OK",
            "요청이 성공적으로 처리되었습니다."
    ),

    CREATED(HttpStatus.CREATED,
            "COMMON_CREATED",
            "리소스가 성공적으로 생성되었습니다."
    ),

    ACCEPTED(HttpStatus.ACCEPTED,
            "COMMON_ACCEPTED",
            "요청이 접수되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
