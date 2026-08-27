package AIFinance.demo.auth.controller;

import AIFinance.demo.auth.dto.*;
import AIFinance.demo.auth.exception.AuthErrorCode;
import AIFinance.demo.auth.service.AuthService;
import AIFinance.demo.global.apiPayload.ApiResponse;
import AIFinance.demo.global.apiPayload.code.GeneralSuccessCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/auth";
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(14);

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(GeneralSuccessCode.CREATED.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.CREATED, response));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(request);
        addRefreshTokenCookie(httpResponse, result.refreshToken());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result.response());
    }

    @GetMapping("/check")
    public ApiResponse<DuplicateCheckResponse> checkDuplicate(
            @RequestParam String type,
            @RequestParam String value
    ) {
        DuplicateCheckResponse response = authService.checkDuplicate(type, value);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenReissueResponse> reissue(
            @CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        TokenReissueResult result = authService.reissue(refreshToken);
        addRefreshTokenCookie(httpResponse, result.refreshToken());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result.response());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        authService.logout(refreshToken);
        expireRefreshTokenCookie(httpResponse);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> getMe() {
        Long userId = currentUserId();
        MeResponse response = authService.getMe(userId);

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long userId)) {
            throw new GeneralException(AuthErrorCode.USER_NOT_FOUND);
        }
        return userId;
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
