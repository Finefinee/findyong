package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.LostItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "분실물 주인 확인 상태 변경 요청")
public class LostItemStatusUpdateRequest {

    @Schema(description = "분실물 상태", example = "OWNER_FOUND")
    private LostItemStatus status;
}
