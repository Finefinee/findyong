package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.LoginRequest;
import com.example.lostfound_project.dto.LoginResponse;
import com.example.lostfound_project.dto.MessageResponse;
import com.example.lostfound_project.dto.RegisterRequest;
import com.example.lostfound_project.dto.UserResponse;
import com.example.lostfound_project.dto.UserUpdateRequest;
import com.example.lostfound_project.model.User;
import com.example.lostfound_project.security.JwtAuthenticationFilter;
import com.example.lostfound_project.security.JwtTokenProvider;
import com.example.lostfound_project.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "User", description = "회원가입 및 로그인 API")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 회원가입
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "아이디 중복 확인 후 비밀번호를 암호화하여 사용자를 등록합니다.")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (!userService.register(request.toEntity())) {
            return ResponseEntity.badRequest().body(new MessageResponse("이미 존재하는 아이디입니다."));
        }

        return ResponseEntity.ok(new MessageResponse("회원가입 성공"));
    }

    // 로그인
    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호를 검증하고 Set-Cookie 헤더로 accessToken HttpOnly JWT 쿠키를 발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "userId": "user1",
                                      "password": "password"
                                    }
                                    """)
                    )
            )
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> user = userService.login(request.getUserId(), request.getPassword());
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(new MessageResponse("아이디 또는 비밀번호가 일치하지 않습니다."));
        }

        ResponseCookie cookie = ResponseCookie.from(
                        JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME,
                        jwtTokenProvider.createToken(user.get().getUserId()))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtTokenProvider.getExpirationMillis() / 1000)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse("로그인 성공", user.get().getUserId()));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "accessToken HttpOnly JWT 쿠키를 만료시켜 로그아웃 처리합니다.")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("로그아웃 성공"));
    }

    @GetMapping("/users")
    @Operation(summary = "회원 목록 조회", description = "회원 목록을 조회합니다. 비밀번호는 응답에 포함하지 않습니다.")
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "회원 상세 조회", description = "회원 ID로 단일 회원 정보를 조회합니다. 비밀번호는 응답에 포함하지 않습니다.")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}")
    @Operation(summary = "회원 수정", description = "JWT 쿠키의 사용자 ID가 대상 회원과 일치할 때만 회원 정보를 수정합니다.")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request,
            Principal principal) {
        try {
            UserService.UpdateResult result = userService.updateUser(id, request, principal.getName());

            if (result.status() == UserService.UpdateStatus.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }

            if (result.status() != UserService.UpdateStatus.SUCCESS) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(result.message()));
            }

            return ResponseEntity.ok(result.user());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "회원 삭제", description = "JWT 쿠키의 사용자 ID가 대상 회원과 일치할 때만 회원을 삭제합니다.")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long id,
            Principal principal) {
        UserService.DeleteResult result = userService.deleteUser(id, principal.getName());

        if (result == UserService.DeleteResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }

        if (result != UserService.DeleteResult.SUCCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(result.getMessage()));
        }

        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }
}
