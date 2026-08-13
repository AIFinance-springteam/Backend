package AIFinance.demo.receipt.exception;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import AIFinance.demo.global.apiPayload.exception.GeneralException;

public class ReceiptItemException extends GeneralException {
  public ReceiptItemException(BaseErrorCode code) {
    super(code);
  }
}