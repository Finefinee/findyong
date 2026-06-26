package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.MessageResponse;
import com.example.lostfound_project.dto.NoticeRequest;
import com.example.lostfound_project.dto.NoticeResponse;
import com.example.lostfound_project.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@Tag(name = "Notice", description = "학교생활 분실물 안내 공지 API")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    @Operation(summary = "공지 등록", description = "분실물 보관 장소나 학교생활 관련 안내 공지를 등록합니다.")
    public ResponseEntity<?> createNotice(@RequestBody NoticeRequest request) {
        try {
            return ResponseEntity.ok(noticeService.createNotice(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "공지 전체 조회", description = "등록된 공지 목록을 최신순으로 조회합니다.")
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }

    @GetMapping("/{id}")
    @Operation(summary = "공지 상세 조회", description = "공지 ID로 단일 공지를 조회합니다.")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        return noticeService.getNotice(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "공지 수정", description = "공지 ID로 기존 공지의 제목, 내용, 작성자, 대상 장소를 수정합니다.")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequest request) {
        try {
            return noticeService.updateNotice(id, request)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공지 삭제", description = "공지 ID로 등록된 공지를 삭제합니다.")
    public ResponseEntity<MessageResponse> deleteNotice(@PathVariable Long id) {
        if (!noticeService.deleteNotice(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new MessageResponse("삭제되었습니다."));
    }
}
