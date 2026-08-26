package AIFinance.demo.receipt.dto;

import AIFinance.demo.receipt.entity.ItemShare;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ItemParticipantsResponse {

    private Long itemId;
    private List<ParticipantDto> participants;

    public static ItemParticipantsResponse of(Long itemId, List<ItemShare> shares) {
        List<ParticipantDto> participants = shares.stream()
                .map(share -> ParticipantDto.builder()
                        .tripMemberId(share.getTripMember().getId())
                        .shareAmount(share.getShareAmount())
                        .build())
                .toList();

        return ItemParticipantsResponse.builder()
                .itemId(itemId)
                .participants(participants)
                .build();
    }

    @Getter
    @Builder
    public static class ParticipantDto {
        private Long tripMemberId;
        private Long shareAmount;
    }
}