package com.example.lostfound_project.service;

import com.example.lostfound_project.model.User;
import com.example.lostfound_project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}
