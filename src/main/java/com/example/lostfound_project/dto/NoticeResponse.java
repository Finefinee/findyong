package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.Notice;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        String writer,
        String targetLocation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getWriter(),
                notice.getTargetLocation(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
