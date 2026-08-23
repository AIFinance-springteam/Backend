package AIFinance.demo.auth.service;

import AIFinance.demo.auth.dto.*;
import AIFinance.demo.auth.exception.AuthErrorCode;
import AIFinance.demo.auth.jwt.JwtProvider;
import AIFinance.demo.auth.repository.RefreshTokenRepository;
import AIFinance.demo.global.apiPayload.code.GeneralErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.user.entity.User;
import AIFinance.demo.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_성공하면_User를_저장하고_응답을_반환한다() {
        SignupRequest request = new SignupRequest("test@test.com", "password1", "tester");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByNickname(request.nickname())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            return savedUser;
        });

        SignupResponse response = authService.signup(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.nickname()).isEqualTo(request.nickname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_이메일이_중복이면_예외를_던지고_저장하지_않는다() {
        SignupRequest request = new SignupRequest("dup@test.com", "password1", "tester");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.EMAIL_DUPLICATE);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_성공하면_토큰을_발급하고_refreshToken을_저장한다() {
        LoginRequest request = new LoginRequest("test@test.com", "password1");
        User user = User.builder()
                .id(1L)
                .email(request.email())
                .password("encoded-password")
                .nickname("tester")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateAccessToken(user.getId())).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(user.getId())).thenReturn("refresh-token");
        when(jwtProvider.getExpiration("refresh-token"))
                .thenReturn(new Date(System.currentTimeMillis() + 1_209_600_000L));

        LoginResult result = authService.login(request);

        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.response().userId()).isEqualTo(user.getId());
        assertThat(result.response().nickname()).isEqualTo(user.getNickname());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_비밀번호가_틀리면_예외를_던지고_토큰을_발급하지_않는다() {
        LoginRequest request = new LoginRequest("test@test.com", "wrong-password");
        User user = User.builder()
                .id(1L)
                .email(request.email())
                .password("encoded-password")
                .nickname("tester")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

        verify(jwtProvider, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_존재하지_않는_이메일이면_예외를_던진다() {
        LoginRequest request = new LoginRequest("noone@test.com", "password1");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void checkDuplicate_EMAIL_타입이면_existsByEmail의_반대값을_반환한다() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

        DuplicateCheckResponse response = authService.checkDuplicate("EMAIL", "test@test.com");

        assertThat(response.available()).isTrue();
    }

    @Test
    void checkDuplicate_NICKNAME_타입이고_이미_사용중이면_available이_false다() {
        when(userRepository.existsByNickname("tester")).thenReturn(true);

        DuplicateCheckResponse response = authService.checkDuplicate("NICKNAME", "tester");

        assertThat(response.available()).isFalse();
    }

    @Test
    void checkDuplicate_지원하지_않는_type이면_BAD_REQUEST_예외를_던진다() {
        assertThatThrownBy(() -> authService.checkDuplicate("INVALID", "value"))
                .isInstanceOf(GeneralException.class)
                .extracting("code")
                .isEqualTo(GeneralErrorCode.BAD_REQUEST);
    }
}
