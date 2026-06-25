package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.NoticeRequest;
import com.example.lostfound_project.dto.NoticeResponse;
import com.example.lostfound_project.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeRequest request) {
        try {
            return ResponseEntity.ok(noticeService.createNotice(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        return noticeService.getNotice(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeRequest request) {
        try {
            return noticeService.updateNotice(id, request)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        if (!noticeService.deleteNotice(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
