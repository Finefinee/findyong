// src/main/java/com/example/lostfound_project/controller/LostFoundController.java
package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.LostItemCreateRequest;
import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.dto.LostItemUpdateRequest;
import com.example.lostfound_project.service.LostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Lost Item", description = "JWT 인증 기반 분실물 등록, 조회, 수정, 삭제 API")
public class LostFoundController {

    private final LostItemService lostItemService;

    public LostFoundController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    // 분실물 등록
    @PostMapping("/lost")
    @Operation(summary = "분실물 등록", description = "로그인한 사용자의 JWT 쿠키를 기준으로 작성자를 저장합니다.")
    public ResponseEntity<?> createLostItem(
            @RequestBody LostItemCreateRequest request,
            Principal principal) {
        try {
            LostItemResponse response = lostItemService.createLostItem(request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // 분실물 전체 조회
    @GetMapping("/lost")
    @Operation(summary = "분실물 목록 조회", description = "분실물 목록을 조회합니다. keyword와 location으로 검색할 수 있습니다.")
    public List<LostItemResponse> getLostItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return lostItemService.getLostItems(keyword, location);
    }

    // 분실물 상세 조회
    @GetMapping("/lost/{id}")
    @Operation(summary = "분실물 상세 조회", description = "분실물 ID로 단일 분실물 정보를 조회합니다.")
    public ResponseEntity<LostItemResponse> getLostItem(@PathVariable Long id) {
        return lostItemService.getLostItem(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 분실물 수정
    @PatchMapping("/lost/{id}")
    @Operation(summary = "분실물 수정", description = "JWT 쿠키의 사용자 ID가 기존 작성자와 일치할 때만 분실물 정보를 수정합니다.")
    public ResponseEntity<?> updateLostItem(
            @PathVariable Long id,
            @RequestBody LostItemUpdateRequest request,
            Principal principal) {
        try {
            LostItemService.UpdateResult result = lostItemService.updateLostItem(id, request, principal.getName());

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
    @Operation(summary = "분실물 삭제", description = "JWT 쿠키의 사용자 ID가 기존 작성자와 일치할 때만 분실물을 삭제합니다.")
    public ResponseEntity<String> deleteLostItem(
            @PathVariable Long id,
            Principal principal) {

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(id, principal.getName());

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
