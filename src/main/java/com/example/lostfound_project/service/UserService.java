package com.example.lostfound_project.service;

import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // 회원가입 처리
    public String register(User user) {
        if (userRepository.existsByUserId(user.getUserId())) {
            return "중복된 아이디입니다.";
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return "success";
    }

    // 로그인 처리
    public boolean login(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return false;

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // 로그인 성공 시 사용자 정보 반환
    public User findUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}
