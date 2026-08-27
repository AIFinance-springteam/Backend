package AIFinance.demo.trip.dto;

import java.time.LocalDateTime;

public record InviteCreateResponse(
        String inviteCode,
        LocalDateTime inviteExpiresAt
) {
}
