package AIFinance.demo.report.service;

import AIFinance.demo.report.dto.ReportResponse;

import java.util.List;

public record ReportAnalysisInput(
        ReportResponse.Summary summary,
        List<ReportResponse.CategoryExpense> categories,
        List<ReportResponse.DailyExpense> dailyExpenses
) {
    public ReportAnalysisInput {
        categories = List.copyOf(categories);
        dailyExpenses = List.copyOf(dailyExpenses);
    }
}
