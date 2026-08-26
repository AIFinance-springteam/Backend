package AIFinance.demo.auth.dto;

public record LoginResult(
        LoginResponse response,
        String refreshToken
) {
}
