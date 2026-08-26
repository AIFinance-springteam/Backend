package AIFinance.demo.global.apiPayload.handler;

import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import AIFinance.demo.global.apiPayload.code.GeneralErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    // 애플리케이션에서 발생하는 커스텀 예외를 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            GeneralException ex
    ) {

        return ResponseEntity.status(ex.getCode().getStatus())
                .body(ApiResponse.onFailure(
                                ex.getCode(),
                                null
                        )
                );
    }

    // 400 에러 구분 
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            Exception ex
    ) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;

        return ResponseEntity
                .status(code.getStatus())
                .body(
                        ApiResponse.onFailure(
                                code,
                                null
                        )
                );
    }


    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception ex
    ) {
        log.error("예상치 못한 서버 오류가 발생했습니다. ", ex);

        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }
}
