package AIFinance.demo.report.service;

import org.springframework.stereotype.Component;

@Component
public class DeterministicReportAnalysisProvider implements ReportAnalysisProvider {

    @Override
    public String analyze(ReportAnalysisInput input) {
        return ReportAnalysisText.generate(input);
    }
}
