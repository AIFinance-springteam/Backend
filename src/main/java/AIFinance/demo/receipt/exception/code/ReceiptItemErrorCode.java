package AIFinance.demo.receipt.exception.code;

import AIFinance.demo.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptItemErrorCode implements BaseErrorCode {
    TRIP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TRIP_NOT_FOUND",
            "여행방을 찾을 수 없습니다."
    ),

    TRIP_MEMBER_REQUIRED(
            HttpStatus.FORBIDDEN,
            "TRIP_MEMBER_REQUIRED",
            "해당 여행방의 참여자만 요청할 수 있습니다."
    ),

    INVALID_TRIP_STATUS(
            HttpStatus.CONFLICT,
            "INVALID_TRIP_STATUS",
            "정산이 진행 중이거나 완료된 여행방은 수정할 수 없습니다."
    ),

    RECEIPT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECEIPT_NOT_FOUND",
            "해당 여행방에서 영수증을 찾을 수 없습니다."
    ),

    RECEIPT_DELETED(
            HttpStatus.CONFLICT,
            "RECEIPT_DELETED",
            "삭제된 영수증에는 추가 비용을 등록할 수 없습니다."
    ),
    RECEIPT_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECEIPT_ITEM_NOT_FOUND",
            "해당 영수증에서 추가 비용 항목을 찾을 수 없습니다."
    ),

    NOT_ADDITIONAL_COST(
            HttpStatus.BAD_REQUEST,
            "NOT_ADDITIONAL_COST",
            "추가 비용 항목만 삭제할 수 있습니다."
    ),

    OCR_FAILED(HttpStatus.BAD_GATEWAY, "RECEIPT_OCR_FAILED", "영수증 OCR 분석에 실패했습니다."),
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_RECEIPT_IMAGE", "영수증 이미지가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
