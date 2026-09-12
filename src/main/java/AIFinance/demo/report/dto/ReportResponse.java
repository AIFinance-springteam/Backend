package AIFinance.demo.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportResponse(
        Long tripId,
        String tripName,
        Summary summary,
        List<CategoryExpense> categories,
        List<DailyExpense> dailyExpenses,
        List<MemberExpense> memberExpenses,
        AiAnalysis aiAnalysis
) {
    public ReportResponse {
        categories = List.copyOf(categories);
        dailyExpenses = List.copyOf(dailyExpenses);
        memberExpenses = List.copyOf(memberExpenses);
    }

    public record Summary(long totalAmount, long averagePerPerson, int paymentCount) {
    }

    public record CategoryExpense(String category, String categoryName, long amount, int percentage) {
    }

    public record DailyExpense(LocalDate date, long amount) {
    }

    public record MemberExpense(Long tripMemberId, String name, long paidAmount, long shareAmount) {
    }

    public record AiAnalysis(String content, LocalDateTime generatedAt) {
    }
}
