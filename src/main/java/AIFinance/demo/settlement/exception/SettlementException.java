package AIFinance.demo.settlement.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;

public class SettlementException extends GeneralException {
    public SettlementException(BaseErrorCode code) {
        super(code);
    }
}
