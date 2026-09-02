package AIFinance.demo.settlement.dto;

import AIFinance.demo.settlement.dto.enums.SettlementCheckIssueType;

import java.util.List;

public class SettlementCheckResponse {

    private SettlementCheckResponse() {
    }

    public record Result(Long tripId, boolean readyToConfirm, Summary summary, List<Issue> issues) {
        public static Result of(Long tripId, int receiptCount, Long totalAmount, List<Issue> issues) {
            List<Issue> copiedIssues = List.copyOf(issues);

            return new Result(tripId, copiedIssues.isEmpty(), new Summary(receiptCount, totalAmount, copiedIssues.size()), copiedIssues);
        }
    }

    public record Summary(int receiptCount, Long totalAmount, int issueCount) {}

    public record Issue(SettlementCheckIssueType type, Long receiptId, Long itemId, String message) {
        public static Issue of(SettlementCheckIssueType type, Long receiptId, Long itemId, String message) {
            return new Issue(type, receiptId, itemId, message);
        }
    }



}
