package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.UserResponse;
import com.example.lostfound_project.model.User;
import com.example.lostfound_project.security.JwtAuthenticationFilter;
import com.example.lostfound_project.security.JwtTokenProvider;
import com.example.lostfound_project.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void loginSetsHttpOnlyJwtCookie() throws Exception {
        User user = new User();
        user.setUserId("user1");
        when(userService.login("user1", "password")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createToken("user1")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationMillis()).thenReturn(3600000L);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user1",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("accessToken=jwt-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void getUsersReturnsUsersWithoutPassword() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(
                new UserResponse(1L, "홍길동", "user1", "2000-01-01")
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user1"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void updateUserReturnsUpdatedUserWithoutPassword() throws Exception {
        UserResponse response = new UserResponse(1L, "새닉네임", "user1", "2000-01-01");
        when(userService.updateUser(eq(1L), any(), eq("user1")))
                .thenReturn(UserService.UpdateResult.success(response));

        mockMvc.perform(patch("/api/users/1")
                        .principal(() -> "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "새닉네임"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void deleteUserReturnsMessage() throws Exception {
        when(userService.deleteUser(1L, "user1")).thenReturn(UserService.DeleteResult.SUCCESS);

        mockMvc.perform(delete("/api/users/1")
                        .principal(() -> "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원이 삭제되었습니다."));
    }
}
