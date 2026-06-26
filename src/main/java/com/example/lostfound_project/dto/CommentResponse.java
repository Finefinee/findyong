package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long itemId,
        String content,
        String writer,
        LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getItemId(),
                comment.getContent(),
                comment.getWriter(),
                comment.getCreatedAt()
        );
    }
}
