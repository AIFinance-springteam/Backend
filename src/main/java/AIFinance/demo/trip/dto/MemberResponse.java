package AIFinance.demo.trip.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        Long tripMemberId,
        Long userId,
        String nickname,
        String role,
        LocalDateTime joinedAt
) {
}
