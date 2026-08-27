package AIFinance.demo.receipt.service;

import AIFinance.demo.global.apiPayload.exception.GeneralException;
import AIFinance.demo.global.exception.SplitErrorCode;
import AIFinance.demo.receipt.dto.ItemParticipantsResponse;
import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.ReceiptItem;
import AIFinance.demo.receipt.entity.enums.SplitType;
import AIFinance.demo.receipt.repository.ItemShareRepository;
import AIFinance.demo.receipt.repository.ReceiptItemRepository;
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

    private ReceiptItem getItem(Long itemId) {
        return receiptItemRepository.findById(itemId)
                .orElseThrow(() -> new GeneralException(SplitErrorCode.ITEM_NOT_FOUND));
    }
}