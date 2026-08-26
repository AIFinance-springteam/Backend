package AIFinance.demo.receipt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemParticipantsRequest {
    private List<Long> tripMemberIds;
}
