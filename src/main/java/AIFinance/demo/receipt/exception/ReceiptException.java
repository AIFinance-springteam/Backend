package AIFinance.demo.receipt.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;

public class ReceiptException extends GeneralException {
    public ReceiptException(BaseErrorCode code) {
        super(code);
    }
}
