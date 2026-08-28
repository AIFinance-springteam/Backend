package AIFinance.demo.receipt.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.global.exception.SplitErrorCode;
import AIFinance.demo.receipt.dto.ItemIndividualRequest;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.dto.ItemRemainderRequest;
import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.SplitType;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
import AIFinance.demo.trip.entity.TripMember;
import AIFinance.demo.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemSplitService {

    private final ReceiptItemRepository receiptItemRepository;
    private final ItemShareRepository itemShareRepository;
    private final TripMemberRepository tripMemberRepository;

    private ReceiptItem getItem(Long itemId) {
        return receiptItemRepository.findById(itemId)
                .orElseThrow(() -> new GeneralException(SplitErrorCode.ITEM_NOT_FOUND));
    }

    public ItemParticipantsResponse splitEqual(Long itemId) {
        ReceiptItem item = getItem(itemId);

        List<ItemShare> shares = itemShareRepository.findByItem_Id(itemId);
        if (shares.isEmpty()) {
            throw new GeneralException(SplitErrorCode.NO_PARTICIPANT_SELECTED);
        }

        shares.sort(Comparator.comparing(share -> share.getTripMember().getId()));

        int count = shares.size();
        long originalAmount = item.getOriginalAmount();
        long baseAmount = originalAmount / count;
        long remainder = originalAmount % count;

        for (int i = 0; i < count; i++) {
            long amount = baseAmount + (i < remainder ? 1 : 0);
            shares.get(i).updateShareAmount(amount);
        }

        item.updateSplitType(SplitType.EQUAL);

        return ItemParticipantsResponse.of(itemId, shares);
    }

    public ItemParticipantsResponse splitRemainder(Long itemId, ItemRemainderRequest request) {
        ReceiptItem item = getItem(itemId);

        List<ItemShare> shares = itemShareRepository.findByItem_Id(itemId);
        if (shares.isEmpty()) {
            throw new GeneralException(SplitErrorCode.NO_PARTICIPANT_SELECTED);
        }

        ItemShare remainderShare = shares.stream()
                .filter(share -> share.getTripMember().getId().equals(request.getTripMemberId()))
                .findFirst()
                .orElseThrow(() -> new GeneralException(SplitErrorCode.PARTICIPANT_NOT_IN_ITEM));

        int count = shares.size();
        long originalAmount = item.getOriginalAmount();
        long baseAmount = originalAmount / count;
        long remainder = originalAmount % count;

        for (ItemShare share : shares) {
            share.updateShareAmount(baseAmount);
        }
        remainderShare.updateShareAmount(baseAmount + remainder);

        item.updateSplitType(SplitType.EQUAL);
        item.updateRemainderMember(remainderShare.getTripMember());

        return ItemParticipantsResponse.of(itemId, shares);
    }

    public ItemParticipantsResponse splitIndividual(Long tripId, Long itemId, ItemIndividualRequest request) {
        ReceiptItem item = getItem(itemId);

        TripMember member = tripMemberRepository.findById(request.getTripMemberId())
                .orElseThrow(() -> new GeneralException(SplitErrorCode.MEMBER_NOT_IN_TRIP));

        if (!member.getTrip().getId().equals(tripId)) {
            throw new GeneralException(SplitErrorCode.MEMBER_NOT_IN_TRIP);
        }

        itemShareRepository.deleteByItem_Id(itemId);

        ItemShare share = ItemShare.of(item, member, item.getOriginalAmount());
        itemShareRepository.save(share);

        item.updateSplitType(SplitType.INDIVIDUAL);
        item.updateRemainderMember(null);

        return ItemParticipantsResponse.of(itemId, List.of(share));
    }
}