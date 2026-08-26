package AIFinance.demo.trip.dto;

import java.time.LocalDate;

public record TripResponse(
        Long tripId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String inviteCode
) {
}
