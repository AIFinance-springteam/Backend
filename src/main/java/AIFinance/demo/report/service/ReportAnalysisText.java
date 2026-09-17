package AIFinance.demo.report.service;

import AIFinance.demo.report.dto.ReportResponse;

import java.text.NumberFormat;
import java.util.Locale;

final class ReportAnalysisText {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private ReportAnalysisText() {
    }

    static String generate(ReportAnalysisInput input) {
        ReportResponse.Summary summary = input.summary();
        if (summary.paymentCount() == 0) {
            return "아직 집계할 수 있는 여행 소비 내역이 없습니다.";
        }

        StringBuilder analysis = new StringBuilder()
                .append("이번 여행의 총지출은 ")
                .append(NUMBER_FORMAT.format(summary.totalAmount()))
                .append("원으로 1인당 평균 ")
                .append(NUMBER_FORMAT.format(summary.averagePerPerson()))
                .append("원입니다. ");

        if (!input.categories().isEmpty()) {
            ReportResponse.CategoryExpense largest = input.categories().get(0);
            analysis.append(largest.categoryName())
                    .append(" 항목이 전체의 ")
                    .append(largest.percentage())
                    .append("%로 가장 큰 비중을 차지했습니다.");

            if (input.categories().size() > 1) {
                analysis.append(" ")
                        .append(input.categories().get(1).categoryName())
                        .append(" 항목이 그 뒤를 이었습니다.");
            }
        } else {
            analysis.append("카테고리가 지정된 지출 항목은 없습니다.");
        }

        return analysis.toString();
    }
}
