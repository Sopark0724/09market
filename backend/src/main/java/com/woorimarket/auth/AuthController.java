package com.woorimarket.auth;

import com.woorimarket.auth.dto.*;
import com.woorimarket.config.JwtTokenProvider;
import com.woorimarket.domain.user.User;
import com.woorimarket.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Mock: 인증번호 저장 (실제로는 Redis 등 사용)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    @PostMapping("/phone/send")
    public ResponseEntity<?> sendPhoneVerification(@RequestBody @Valid PhoneSendRequest request) {
        // Mock: 항상 123456을 인증번호로 설정
        String code = "123456";
        verificationCodes.put(request.getPhone(), code);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "인증번호가 발송되었습니다. (Mock: " + code + ")");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<?> verifyPhone(@RequestBody @Valid PhoneVerifyRequest request) {
        String storedCode = verificationCodes.get(request.getPhone());

        if (storedCode == null || !storedCode.equals(request.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "인증번호가 일치하지 않습니다."));
        }

        verificationCodes.remove(request.getPhone());

        // 기존 유저가 있으면 로그인, 없으면 회원가입 필요 표시
        return userRepository.findByPhone(request.getPhone())
                .map(user -> {
                    String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "registered", true,
                            "token", token,
                            "user", mapUser(user)
                    ));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "registered", false,
                        "phone", request.getPhone()
                )));
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody @Valid KakaoLoginRequest request) {
        // Mock: 카카오 로그인 시뮬레이션
        // 실제로는 카카오 API로 access_token 검증 후 사용자 정보 조회
        String mockKakaoId = "kakao_" + request.getAccessToken().hashCode();

        return userRepository.findByKakaoId(mockKakaoId)
                .map(user -> {
                    String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "registered", true,
                            "token", token,
                            "user", mapUser(user)
                    ));
                })
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "registered", false,
                        "kakaoId", mockKakaoId,
                        "email", "mock@kakao.com",
                        "name", "카카오사용자"
                )));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        // 중복 체크
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 등록된 이메일입니다."));
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 등록된 전화번호입니다."));
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(User.Role.valueOf(request.getRole()))
                .authProvider(User.AuthProvider.valueOf(request.getAuthProvider()))
                .kakaoId(request.getKakaoId())
                .build();

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", mapUser(user)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute(value = "user", required = false) User user) {
        if (user == null) {
            // SecurityContext에서 가져오기
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                user = (User) auth.getPrincipal();
            }
        }

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "인증이 필요합니다."));
        }

        return ResponseEntity.ok(Map.of("success", true, "user", mapUser(user)));
    }

    private Map<String, Object> mapUser(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("name", user.getName());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole().name());
        map.put("authProvider", user.getAuthProvider().name());
        return map;
    }
}
