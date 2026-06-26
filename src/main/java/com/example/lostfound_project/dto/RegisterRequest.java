package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class RegisterRequest {

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "로그인에 사용할 사용자 ID", example = "user1")
    private String userId;

    @Schema(description = "비밀번호", example = "password")
    private String password;

    @Schema(description = "생년월일", example = "2000-01-01")
    private String birth;

    public User toEntity() {
        User user = new User();
        user.setNickname(nickname);
        user.setUserId(userId);
        user.setPassword(password);
        user.setBirth(birth);
        return user;
    }
}
