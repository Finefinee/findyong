package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "분실물 수정 요청")
public class LostItemUpdateRequest {

    @Schema(description = "수정할 분실물 이름", example = "검은색 카드지갑")
    private String itemName;

    @Schema(description = "수정할 상세 설명", example = "학생증이 들어있는 카드지갑입니다.")
    private String description;

    @Schema(description = "수정할 분실 장소", example = "학생회관")
    private String location;

    @Schema(description = "수정할 분실 시간", example = "2026-06-24T10:00:00")
    private LocalDateTime lostTime;

    @Schema(description = "작성자 검증용 사용자 ID", example = "user1")
    private String userId;

    @Schema(description = "익명 글 검증용 비밀번호", example = "1234")
    private String password;
}
