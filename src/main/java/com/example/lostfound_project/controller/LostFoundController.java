// src/main/java/com/example/lostfound_project/controller/LostFoundController.java
package com.example.lostfound_project.controller;

import com.example.lostfound_project.model.LostItem;
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
    public ResponseEntity<?> createLostItem(@RequestBody LostItem item) {
        try {
            LostItem saved = lostItemService.createLostItem(item);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 분실물 전체 조회
    @GetMapping("/lost")
    public List<LostItem> getAllLostItems() {
        return lostItemService.getAllLostItems();
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
