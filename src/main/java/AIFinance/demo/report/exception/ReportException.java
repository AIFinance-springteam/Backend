package AIFinance.demo.report.exception;

import AIFinance.demo.global.apiPayload.exception.GeneralException;

public class ReportException extends GeneralException {
    public ReportException(ReportErrorCode code) {
        super(code);
    }
}
