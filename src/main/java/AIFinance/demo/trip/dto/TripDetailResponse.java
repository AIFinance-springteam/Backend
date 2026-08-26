package AIFinance.demo.trip.dto;

import java.time.LocalDate;
import java.util.List;

public record TripDetailResponse(
        Long tripId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long ownerId,
        String ownerNickname,
        List<MemberResponse> members
) {
}
