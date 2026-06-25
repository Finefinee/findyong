package com.example.lostfound_project.dto;

import com.example.lostfound_project.model.Notice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeRequest {

    private String title;
    private String content;
    private String writer;
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
