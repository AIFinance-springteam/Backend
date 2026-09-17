package AIFinance.demo.receipt.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.global.exception.SplitErrorCode;
import AIFinance.demo.receipt.dto.ItemParticipantsRequest;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.entity.enums.TripMemberStatus;
import AIFinance.demo.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemParticipantService {

    private final ReceiptItemRepository receiptItemRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ItemShareRepository itemShareRepository;

    public ItemParticipantsResponse selectParticipants(Long tripId, Long itemId, ItemParticipantsRequest request) {
        List<Long> tripMemberIds = request.getTripMemberIds();
        if (tripMemberIds == null || tripMemberIds.isEmpty()) {
            throw new GeneralException(SplitErrorCode.NO_PARTICIPANT_SELECTED);
        }

        ReceiptItem item = getItem(itemId);
        List<TripMember> members = tripMemberRepository.findAllById(tripMemberIds);
        validateMembersBelongToTrip(members, tripMemberIds, tripId);

        List<ItemShare> shares = replaceShares(item, members);
        return ItemParticipantsResponse.of(itemId, shares);
    }

    public ItemParticipantsResponse selectAllParticipants(Long tripId, Long itemId) {
        ReceiptItem item = getItem(itemId);

        List<TripMember> members = tripMemberRepository.findByTrip_IdAndStatus(tripId, TripMemberStatus.ACTIVE);
        if (members.isEmpty()) {
            throw new GeneralException(SplitErrorCode.NO_PARTICIPANT_SELECTED);
        }

        List<ItemShare> shares = replaceShares(item, members);
        return ItemParticipantsResponse.of(itemId, shares);
    }

    private ReceiptItem getItem(Long itemId) {
        return receiptItemRepository.findById(itemId)
                .orElseThrow(() -> new GeneralException(SplitErrorCode.ITEM_NOT_FOUND));
    }

    private List<ItemShare> replaceShares(ReceiptItem item, List<TripMember> members) {
        itemShareRepository.deleteByItem_Id(item.getId());

        List<ItemShare> shares = members.stream()
                .map(member -> ItemShare.of(item, member, 0L))
                .toList();

        return itemShareRepository.saveAll(shares);
    }

    private void validateMembersBelongToTrip(List<TripMember> members, List<Long> requestedIds, Long tripId) {
        if (members.size() != requestedIds.size()) {
            throw new GeneralException(SplitErrorCode.MEMBER_NOT_IN_TRIP);
        }
        boolean allBelong = members.stream()
                .allMatch(member -> member.getTrip().getId().equals(tripId));
        if (!allBelong) {
            throw new GeneralException(SplitErrorCode.MEMBER_NOT_IN_TRIP);
        }
    }
}
