package org.example.expert.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    @Pattern(
        regexp = "^[a-zA-Z0-9-_]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
        message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}",
        message = "비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.")
    private String password;

    @NotBlank
    private String userRole;
}
