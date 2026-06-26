package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "사용자 ID", example = "user1")
    private String userId;

    @Schema(description = "비밀번호", example = "password")
    private String password;
}
