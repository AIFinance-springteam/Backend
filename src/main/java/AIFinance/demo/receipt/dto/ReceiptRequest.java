package AIFinance.demo.receipt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class ReceiptRequest {

    private ReceiptRequest() {
    }

    public record UploadImage(
            @NotBlank(message = "이미지 URL은 필수입니다.")
            String imageUrl
    ) {
    }

    public record CreateManual(
            @NotBlank(message = "가맹점명은 필수입니다.")
            String merchantName,

            @NotNull(message = "결제 일시는 필수입니다.")
            LocalDateTime paidAt,

            @NotNull(message = "총 금액은 필수입니다.")
            @Positive(message = "총 금액은 1원 이상이어야 합니다.")
            Long totalAmount
    ) {
    }

    public record ChangePayer(
            @NotNull(message = "결제자 멤버 ID는 필수입니다.")
            Long payerMemberId
    ) {
    }

    public record UpdateInfo(
            String merchantName,
            LocalDateTime paidAt,
            Long totalAmount
    ) {
    }
}
