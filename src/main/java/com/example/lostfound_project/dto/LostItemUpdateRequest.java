package com.example.lostfound_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LostItemUpdateRequest {

    private String itemName;
    private String description;
    private String location;
    private LocalDateTime lostTime;
    private String userId;
    private String password;
}
