package AIFinance.demo.trip.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardAmountResponse {

    private Long amount;

    public static DashboardAmountResponse of(Long amount) {
        return DashboardAmountResponse.builder()
                .amount(amount)
                .build();
    }
}