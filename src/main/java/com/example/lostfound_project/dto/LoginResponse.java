package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "응답 메시지", example = "로그인 성공")
        String message,

        @Schema(description = "로그인한 사용자 ID", example = "user1")
        String userId
) {
}
