package AIFinance.demo.auth.service;

import AIFinance.demo.auth.dto.*;
import AIFinance.demo.auth.entity.RefreshToken;
import AIFinance.demo.auth.exception.AuthErrorCode;
import AIFinance.demo.auth.jwt.JwtProvider;
import AIFinance.demo.auth.repository.RefreshTokenRepository;
import AIFinance.demo.global.apiPayload.code.GeneralErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.user.entity.User;
import AIFinance.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new GeneralException(AuthErrorCode.EMAIL_DUPLICATE);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new GeneralException(AuthErrorCode.NICKNAME_DUPLICATE);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);

        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String rawRefreshToken = jwtProvider.generateRefreshToken(user.getId());
        saveRefreshToken(user, rawRefreshToken);

        LoginResponse response = new LoginResponse(accessToken, user.getId(), user.getNickname());
        return new LoginResult(response, rawRefreshToken);
    }

    public DuplicateCheckResponse checkDuplicate(String type, String value) {
        boolean exists = switch (type.toUpperCase()) {
            case "EMAIL" -> userRepository.existsByEmail(value);
            case "NICKNAME" -> userRepository.existsByNickname(value);
            default -> throw new GeneralException(GeneralErrorCode.BAD_REQUEST);
        };

        return new DuplicateCheckResponse(!exists);
    }

    @Transactional
    public TokenReissueResult reissue(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(hashToken(rawRefreshToken))
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        stored.revoke();

        User user = stored.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user.getId());
        String newRawRefreshToken = jwtProvider.generateRefreshToken(user.getId());
        saveRefreshToken(user, newRawRefreshToken);

        TokenReissueResponse response = new TokenReissueResponse(newAccessToken);
        return new TokenReissueResult(response, newRawRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            return;
        }

        refreshTokenRepository.findByToken(hashToken(rawRefreshToken))
                .ifPresent(refreshToken -> refreshTokenRepository.deleteAllByUser_Id(refreshToken.getUser().getId()));
    }

    public MeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.USER_NOT_FOUND));

        return new MeResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    private void saveRefreshToken(User user, String rawRefreshToken) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtProvider.getExpiration(rawRefreshToken).toInstant(),
                ZoneId.systemDefault()
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashToken(rawRefreshToken))
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
