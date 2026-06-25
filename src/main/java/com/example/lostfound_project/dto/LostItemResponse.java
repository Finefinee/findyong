package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.LostItem;

import java.time.LocalDateTime;

public record LostItemResponse(
        Long id,
        String itemName,
        String description,
        String location,
        LocalDateTime lostTime,
        String writer
) {

    public static LostItemResponse from(LostItem item) {
        return new LostItemResponse(
                item.getId(),
                item.getItemName(),
                item.getDescription(),
                item.getLocation(),
                item.getLostTime(),
                item.getWriter()
        );
    }
}
