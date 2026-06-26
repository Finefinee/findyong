package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.UserResponse;
import com.example.lostfound_project.dto.UserUpdateRequest;
import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public Optional<UserResponse> getUser(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from);
    }

    public UpdateResult updateUser(Long id, UserUpdateRequest request, String requesterUserId) {
        validateUpdateRequest(request);

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return UpdateResult.notFound();
        }

        if (!isSameUser(user, requesterUserId)) {
            return UpdateResult.notOwner();
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getBirth() != null) {
            user.setBirth(request.getBirth().trim());
        }

        return UpdateResult.success(UserResponse.from(userRepository.save(user)));
    }

    public DeleteResult deleteUser(Long id, String requesterUserId) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return DeleteResult.NOT_FOUND;
        }

        if (!isSameUser(user, requesterUserId)) {
            return DeleteResult.NOT_OWNER;
        }

        userRepository.deleteById(id);
        return DeleteResult.SUCCESS;
    }

    private boolean isSameUser(User user, String requesterUserId) {
        return Objects.equals(user.getUserId(), requesterUserId);
    }

    private void validateUpdateRequest(UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if (request.getNickname() == null
                && request.getPassword() == null
                && request.getBirth() == null) {
            throw new IllegalArgumentException("수정할 내용이 필요합니다.");
        }

        if (request.getNickname() != null && request.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 비워둘 수 없습니다.");
        }

        if (request.getPassword() != null && request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비워둘 수 없습니다.");
        }

        if (request.getBirth() != null && request.getBirth().isBlank()) {
            throw new IllegalArgumentException("생년월일은 비워둘 수 없습니다.");
        }
    }

    public record UpdateResult(UpdateStatus status, String message, UserResponse user) {

        public static UpdateResult success(UserResponse user) {
            return new UpdateResult(UpdateStatus.SUCCESS, "회원 정보가 수정되었습니다.", user);
        }

        public static UpdateResult notFound() {
            return new UpdateResult(UpdateStatus.NOT_FOUND, null, null);
        }

        public static UpdateResult notOwner() {
            return new UpdateResult(UpdateStatus.NOT_OWNER, "본인 정보만 수정할 수 있습니다.", null);
        }
    }

    public enum UpdateStatus {
        SUCCESS,
        NOT_FOUND,
        NOT_OWNER
    }

    public enum DeleteResult {
        SUCCESS("회원이 삭제되었습니다."),
        NOT_FOUND(null),
        NOT_OWNER("본인 정보만 삭제할 수 있습니다.");

        private final String message;

        DeleteResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
