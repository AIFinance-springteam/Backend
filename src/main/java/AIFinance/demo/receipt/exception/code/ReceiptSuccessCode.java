package AIFinance.demo.receipt.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptSuccessCode implements BaseSuccessCode {

    RECEIPT_UPLOADED(
            HttpStatus.CREATED,
            "RECEIPT_UPLOADED",
            "영수증 이미지가 업로드되었습니다."
    ),

    RECEIPT_MANUAL_CREATED(
            HttpStatus.CREATED,
            "RECEIPT_MANUAL_CREATED",
            "영수증이 직접 등록되었습니다."
    ),

    RECEIPT_PAYER_CHANGED(
            HttpStatus.OK,
            "RECEIPT_PAYER_CHANGED",
            "결제자가 변경되었습니다."
    ),

    RECEIPT_UPDATED(
            HttpStatus.OK,
            "RECEIPT_UPDATED",
            "영수증 정보가 수정되었습니다."
    ),

    RECEIPT_DELETED(
            HttpStatus.OK,
            "RECEIPT_DELETED",
            "영수증이 삭제되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
