package AIFinance.demo.receipt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ReceiptItemRequest {

    private ReceiptItemRequest() {
    }

    public record CreateAdditionalCost(
            @NotBlank(message = "항목명은 필수입니다.")
            @Size(max = 100, message = "항목명은 100자 이하여야 합니다.")
            String itemName,

            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "금액은 1원 이상이어야 합니다.")
            Long amount
    ) {
    }
}