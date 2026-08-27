package AIFinance.demo.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTripRequest(
        @NotBlank
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}
