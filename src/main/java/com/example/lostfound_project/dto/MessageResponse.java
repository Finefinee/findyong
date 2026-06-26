package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 응답")
public record MessageResponse(
        @Schema(description = "응답 메시지", example = "처리되었습니다.")
        String message
) {
}
