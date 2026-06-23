package com.example.lostfound_project.controller;

import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUserId(user.getUserId())) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String password = request.get("password");

        User user = userRepository.findByUserId(userId);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // JSON 응답
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그인 성공");
        response.put("userId", user.getUserId());

        return ResponseEntity.ok(response);
    }
}
