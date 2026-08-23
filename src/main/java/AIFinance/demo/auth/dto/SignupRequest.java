package AIFinance.demo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @Email
        @NotBlank
        String email,

        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "비밀번호는 영문과 숫자를 포함하여 8자 이상이어야 합니다."
        )
        String password,

        @NotBlank
        String nickname
) {
}
