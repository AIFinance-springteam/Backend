package AIFinance.demo.trip.dto;

import java.time.LocalDate;

public record TripSummaryResponse(
        Long tripId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        int memberCount
) {
}
