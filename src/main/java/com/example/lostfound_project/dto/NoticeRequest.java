package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지 등록/수정 요청")
public class NoticeRequest {

    @Schema(description = "공지 제목", example = "도서관 분실물 보관 안내")
    private String title;

    @Schema(description = "공지 내용", example = "도서관에서 발견된 분실물은 1층 행정실에 보관합니다.")
    private String content;

    @Schema(description = "작성자", example = "admin")
    private String writer;

    @Schema(description = "공지 대상 장소", example = "도서관")
    private String targetLocation;

    public Notice toEntity() {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setWriter(writer);
        notice.setTargetLocation(targetLocation);
        return notice;
    }
}
