package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.UserResponse;
import com.example.lostfound_project.dto.UserUpdateRequest;
import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerReturnsFalseWhenUserIdAlreadyExists() {
        User user = new User();
        user.setUserId("user1");
        when(userRepository.existsByUserId("user1")).thenReturn(true);

        boolean registered = userService.register(user);

        assertThat(registered).isFalse();
        verify(passwordEncoder, never()).encode(user.getPassword());
        verify(userRepository, never()).save(user);
    }

    @Test
    void registerEncodesPasswordAndSavesUser() {
        User user = new User();
        user.setUserId("user1");
        user.setPassword("raw-password");
        when(userRepository.existsByUserId("user1")).thenReturn(false);
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        boolean registered = userService.register(user);

        assertThat(registered).isTrue();
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        verify(userRepository).save(user);
    }

    @Test
    void loginReturnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findByUserId("user1")).thenReturn(null);

        Optional<User> user = userService.login("user1", "password");

        assertThat(user).isEmpty();
        verify(passwordEncoder, never()).matches("password", null);
    }

    @Test
    void loginReturnsEmptyWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setPassword("encoded-password");
        when(userRepository.findByUserId("user1")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        Optional<User> result = userService.login("user1", "wrong-password");

        assertThat(result).isEmpty();
    }

    @Test
    void loginReturnsUserWhenPasswordMatches() {
        User user = new User();
        user.setUserId("user1");
        user.setPassword("encoded-password");
        when(userRepository.findByUserId("user1")).thenReturn(user);
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        Optional<User> result = userService.login("user1", "raw-password");

        assertThat(result).contains(user);
    }

    @Test
    void userResponseDoesNotExposePassword() {
        List<String> fields = Arrays.stream(UserResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(fields).doesNotContain("password");
    }

    @Test
    void getUsersReturnsResponsesWithoutPassword() {
        User user = new User();
        user.setId(1L);
        user.setNickname("홍길동");
        user.setUserId("user1");
        user.setPassword("encoded-password");
        user.setBirth("2000-01-01");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> users = userService.getUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).userId()).isEqualTo("user1");
    }

    @Test
    void updateUserRejectsOtherUser() {
        User user = new User();
        user.setUserId("user1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");

        UserService.UpdateResult result = userService.updateUser(1L, request, "user2");

        assertThat(result.status()).isEqualTo(UserService.UpdateStatus.NOT_OWNER);
        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUserEncodesPasswordAndSaves() {
        User user = new User();
        user.setId(1L);
        user.setNickname("홍길동");
        user.setUserId("user1");
        user.setPassword("old-password");
        user.setBirth("2000-01-01");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userRepository.save(user)).thenReturn(user);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");
        request.setPassword("new-password");

        UserService.UpdateResult result = userService.updateUser(1L, request, "user1");

        assertThat(result.status()).isEqualTo(UserService.UpdateStatus.SUCCESS);
        assertThat(result.user().nickname()).isEqualTo("새닉네임");
        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserDeletesWhenOwnerMatches() {
        User user = new User();
        user.setUserId("user1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserService.DeleteResult result = userService.deleteUser(1L, "user1");

        assertThat(result).isEqualTo(UserService.DeleteResult.SUCCESS);
        verify(userRepository).deleteById(1L);
    }
}
