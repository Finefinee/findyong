package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.LostItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LostItemCreateRequest {

    private String itemName;
    private String description;
    private String location;
    private LocalDateTime lostTime;
    private String writer;
    private String password;

    public LostItem toEntity() {
        LostItem item = new LostItem();
        item.setItemName(itemName);
        item.setDescription(description);
        item.setLocation(location);
        item.setLostTime(lostTime);
        item.setWriter(writer);
        item.setPassword(password);
        return item;
    }
}
