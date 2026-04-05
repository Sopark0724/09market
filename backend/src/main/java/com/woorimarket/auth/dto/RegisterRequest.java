package com.woorimarket.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String phone;

    @NotBlank(message = "역할은 필수입니다.")
    private String role; // SELLER or BUYER

    @NotBlank(message = "인증 방식은 필수입니다.")
    private String authProvider; // KAKAO or PHONE

    private String kakaoId;
}
