// 현재 로그인한 유저가 누구인지 어디서든 꺼내쓸 수 있는 공용유틸 파일입니다.
package AIFinance.demo.global.security;

import AIFinance.demo.global.apiPayload.code.GeneralErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long userId)) {
            throw new GeneralException(GeneralErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}