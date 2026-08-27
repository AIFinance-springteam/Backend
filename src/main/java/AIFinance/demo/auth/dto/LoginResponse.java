package AIFinance.demo.auth.dto;

public record LoginResponse(
        String accessToken,
        Long userId,
        String nickname
) {
}
