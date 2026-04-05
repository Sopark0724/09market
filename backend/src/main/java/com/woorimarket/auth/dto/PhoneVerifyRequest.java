package com.woorimarket.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PhoneVerifyRequest {
    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String code;
}
