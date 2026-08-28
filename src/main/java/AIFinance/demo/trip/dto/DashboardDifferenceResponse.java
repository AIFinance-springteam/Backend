package AIFinance.demo.trip.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardDifferenceResponse {

    private Long myPayments;
    private Long myShares;
    private Long difference;
    private Boolean isFinal;

    public static DashboardDifferenceResponse of(Long myPayments, Long myShares, Boolean isFinal) {
        return DashboardDifferenceResponse.builder()
                .myPayments(myPayments)
                .myShares(myShares)
                .difference(myPayments - myShares)
                .isFinal(isFinal)
                .build();
    }
}