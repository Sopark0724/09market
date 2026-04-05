package com.woorimarket.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class KakaoLoginRequest {
    @NotBlank(message = "Access token은 필수입니다.")
    private String accessToken;
}
