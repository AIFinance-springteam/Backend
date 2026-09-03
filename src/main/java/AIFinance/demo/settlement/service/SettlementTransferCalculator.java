package AIFinance.demo.settlement.service;

import AIFinance.demo.receipt.entity.ItemShare;
import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.settlement.entity.Settlement;
import AIFinance.demo.settlement.entity.SettlementTransfer;
import AIFinance.demo.settlement.exception.SettlementException;
import AIFinance.demo.settlement.exception.code.SettlementErrorCode;
import AIFinance.demo.trip.entity.TripMember;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SettlementTransferCalculator {

    public List<SettlementTransfer> calculate(Settlement settlement, List<TripMember> members, List<Receipt> receipts, List<ItemShare> shares) {
        Map<Long, TripMember> membersById = members.stream().collect(Collectors.toMap(TripMember::getId, Function.identity()));

        Map<Long, Long> balances = initializeBalances(members);
        addPayments(balances, membersById, receipts);
        subtractBurdens(balances, membersById, shares);
        validateBalanceTotal(balances);

        return createTransfers(settlement, membersById, balances);
    }

    private Map<Long, Long> initializeBalances(List<TripMember> members) {
        Map<Long, Long> balances = new HashMap<>();

        for (TripMember member : members) {
            balances.put(member.getId(), 0L);
        }
        return balances;
    }

    private void addPayments(Map<Long, Long> balances, Map<Long, TripMember> membersById, List<Receipt> receipts) {
        for (Receipt receipt : receipts) {
            Long payerId = receipt.getPayerMember().getId();

            validateActiveMember(membersById, payerId);
            balances.merge(payerId, receipt.getTotalAmount(), Long::sum);

        }
    }

    private void subtractBurdens(Map<Long, Long> balances, Map<Long, TripMember> membersById, List<ItemShare> shares) {
        for (ItemShare share : shares) {
            Long memberId = share.getTripMember().getId();
            validateActiveMember(membersById, memberId);
            balances.merge(memberId, -share.getShareAmount(), Long::sum);
        }
    }

    private void validateActiveMember(Map<Long, TripMember> membersById, Long memberId) {
        if (!membersById.containsKey(memberId)) {
            throw new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_READY);
        }
    }

    private void validateBalanceTotal(Map<Long, Long> balances) {
        long total = balances.values().stream().mapToLong(Long::longValue).sum();

        if (total != 0L) {
            throw new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_READY);
        }
    }

    private List<SettlementTransfer> createTransfers(Settlement settlement, Map<Long, TripMember> membersById, Map<Long, Long> balances) {
        List<Long> senders = findMemberIdsByBalance(balances, false);
        List<Long> receivers = findMemberIdsByBalance(balances, true);
        List<SettlementTransfer> transfers = new ArrayList<>();

        int senderIndex = 0;
        int receiverIndex = 0;

        while (senderIndex < senders.size() && receiverIndex < receivers.size()) {
            Long senderId = senders.get(senderIndex);
            Long receiverId = receivers.get(receiverIndex);

            long amount = Math.min(-balances.get(senderId), balances.get(receiverId));

            transfers.add(SettlementTransfer.create(settlement, membersById.get(senderId), membersById.get(receiverId), amount));

            balances.merge(senderId, amount, Long::sum);
            balances.merge(receiverId, -amount, Long::sum);

            if (balances.get(senderId) == 0L) {
                senderIndex++;
            }

            if (balances.get(receiverId) == 0L) {
                receiverIndex++;
            }
        }

        return transfers;
    }

    private List<Long> findMemberIdsByBalance(Map<Long, Long> balances, boolean positive) {
        return balances.entrySet().stream()
                .filter(entry -> positive ? entry.getValue() > 0L : entry.getValue() < 0L)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

}
