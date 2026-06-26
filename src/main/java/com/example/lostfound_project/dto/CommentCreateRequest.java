package com.example.lostfound_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "댓글 등록 요청")
public class CommentCreateRequest {

    @Schema(description = "댓글 내용", example = "학생회관에서 비슷한 물건을 봤습니다.")
    private String content;
}
