package AIFinance.demo.auth.dto;

public record TokenReissueResult(
        TokenReissueResponse response,
        String refreshToken
) {
}
