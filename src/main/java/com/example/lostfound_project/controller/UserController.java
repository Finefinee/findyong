package com.example.lostfound_project.controller;

import com.example.lostfound_project.model.User;
import com.example.lostfound_project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "User", description = "회원가입 및 로그인 API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "아이디 중복 확인 후 비밀번호를 암호화하여 사용자를 등록합니다.")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (!userService.register(user)) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 검증하고 로그인 결과를 반환합니다.")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String password = request.get("password");

        Optional<User> user = userService.login(userId, password);
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // JSON 응답
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그인 성공");
        response.put("userId", user.get().getUserId());

        return ResponseEntity.ok(response);
    }
}
