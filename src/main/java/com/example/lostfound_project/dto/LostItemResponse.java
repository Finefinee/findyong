package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.model.LostItemStatus;

import java.time.LocalDateTime;

public record LostItemResponse(
        Long id,
        String itemName,
        String description,
        String location,
        LocalDateTime lostTime,
        String writer,
        LostItemStatus status
) {

    public static LostItemResponse from(LostItem item) {
        return new LostItemResponse(
                item.getId(),
                item.getItemName(),
                item.getDescription(),
                item.getLocation(),
                item.getLostTime(),
                item.getWriter(),
                item.getStatus()
        );
    }
}
