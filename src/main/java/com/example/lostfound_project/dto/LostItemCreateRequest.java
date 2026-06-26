package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.LostItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "분실물 등록 요청. 작성자는 JWT 쿠키에서 서버가 자동으로 설정합니다.")
public class LostItemCreateRequest {

    @Schema(description = "분실물 이름", example = "검은색 지갑")
    private String itemName;

    @Schema(description = "분실물 상세 설명", example = "학생증과 카드가 들어있는 검은색 지갑입니다.")
    private String description;

    @Schema(description = "분실 장소", example = "도서관")
    private String location;

    @Schema(description = "분실 시간", example = "2026-06-24T10:00:00")
    private LocalDateTime lostTime;

    public LostItem toEntity(String writer) {
        LostItem item = new LostItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setLostTime(lostTime);
        item.setWriter(writer);
        return item;
    }
}
