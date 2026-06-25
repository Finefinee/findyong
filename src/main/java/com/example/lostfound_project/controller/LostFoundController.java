// src/main/java/com/example/lostfound_project/controller/LostFoundController.java
package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.LostItemCreateRequest;
import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.dto.LostItemUpdateRequest;
import com.example.lostfound_project.service.LostItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LostFoundController {

    private final LostItemService lostItemService;

    public LostFoundController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    // 분실물 등록
    @PostMapping("/lost")
    public ResponseEntity<?> createLostItem(@RequestBody LostItemCreateRequest request) {
        try {
            LostItemResponse response = lostItemService.createLostItem(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 분실물 전체 조회
    @GetMapping("/lost")
    public List<LostItemResponse> getLostItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return lostItemService.getLostItems(keyword, location);
    }

    // 분실물 상세 조회
    @GetMapping("/lost/{id}")
    public ResponseEntity<LostItemResponse> getLostItem(@PathVariable Long id) {
        return lostItemService.getLostItem(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 분실물 수정
    @PatchMapping("/lost/{id}")
    public ResponseEntity<?> updateLostItem(
            @PathVariable Long id,
            @RequestBody LostItemUpdateRequest request) {
        try {
            LostItemService.UpdateResult result = lostItemService.updateLostItem(id, request);

            if (result.status() == LostItemService.UpdateStatus.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }

            if (result.status() != LostItemService.UpdateStatus.SUCCESS) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(result.message());
            }

            return ResponseEntity.ok(result.item());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 분실물 삭제
    @DeleteMapping("/lost/{id}")
    public ResponseEntity<String> deleteLostItem(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(id, payload);

        if (result == LostItemService.DeleteResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }

        if (result != LostItemService.DeleteResult.SUCCESS) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(result.getMessage());
        }

        return ResponseEntity.ok(result.getMessage());
    }
}
