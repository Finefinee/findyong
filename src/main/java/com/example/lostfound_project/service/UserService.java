package com.example.lostfound_project.service;

import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 처리
    public boolean register(User user) {
        if (userRepository.existsByUserId(user.getUserId())) {
            return false;
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return true;
    }

    // 로그인 처리
    public Optional<User> login(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    // 로그인 성공 시 사용자 정보 반환
    public User findUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}
