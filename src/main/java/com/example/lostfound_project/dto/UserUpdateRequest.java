package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원 수정 요청")
public class UserUpdateRequest {

    @Schema(description = "수정할 닉네임", example = "새닉네임")
    private String nickname;

    @Schema(description = "수정할 비밀번호", example = "new-password")
    private String password;

    @Schema(description = "수정할 생년월일", example = "2000-01-01")
    private String birth;
}
