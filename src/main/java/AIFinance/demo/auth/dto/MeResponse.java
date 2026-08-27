package AIFinance.demo.auth.dto;

public record MeResponse(
        Long userId,
        String email,
        String nickname
) {
}
