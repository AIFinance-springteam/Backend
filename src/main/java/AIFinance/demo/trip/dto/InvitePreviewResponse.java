package AIFinance.demo.trip.dto;

import java.time.LocalDate;

public record InvitePreviewResponse(
        Long tripId,
        String tripName,
        LocalDate startDate,
        LocalDate endDate,
        int memberCount,
        String inviterNickname
) {
}
