// src/main/java/com/example/lostfound_project/controller/LostFoundController.java
package com.example.lostfound_project.controller;

import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.repository.LostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LostFoundController {

    private final LostItemRepository lostItemRepository;

    @Autowired
    public LostFoundController(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    // 분실물 등록
    @PostMapping("/lost")
    public ResponseEntity<?> createLostItem(@RequestBody LostItem item) {
        // 익명일 때 비밀번호 필수
        if ("익명".equals(item.getWriter())
                && (item.getPassword() == null || item.getPassword().isEmpty())) {
            return ResponseEntity
                    .badRequest()
                    .body("익명 글은 비밀번호를 설정해야 합니다.");
        }
        LostItem saved = lostItemRepository.save(item);
        return ResponseEntity.ok(saved);
    }

    // 분실물 전체 조회
    @GetMapping("/lost")
    public List<LostItem> getAllLostItems() {
        return lostItemRepository.findAll();
    }

    // 분실물 삭제
    @DeleteMapping("/lost/{id}")
    public ResponseEntity<String> deleteLostItem(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        if ("익명".equals(item.getWriter())) {
            String pw = payload.get("password");
            if (pw == null || !pw.equals(item.getPassword())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("비밀번호가 틀렸습니다.");
            }
        } else {
            String userId = payload.get("userId");
            if (!item.getWriter().equals(userId)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("작성자만 삭제할 수 있습니다.");
            }
        }

        lostItemRepository.deleteById(id);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}
