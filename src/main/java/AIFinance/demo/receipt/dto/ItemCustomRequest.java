package AIFinance.demo.receipt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemCustomRequest {

    private List<CustomShare> shares;

    @Getter
    @NoArgsConstructor
    public static class CustomShare {
        private Long tripMemberId;
        private Long amount;
    }
}