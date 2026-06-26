package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.User;

public record UserResponse(
        Long id,
        String nickname,
        String userId,
        String birth
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getUserId(),
                user.getBirth()
        );
    }
}
