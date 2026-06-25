package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.LostItemCreateRequest;
import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.dto.LostItemUpdateRequest;
import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LostItemService {

    private static final String ANONYMOUS_WRITER = "익명";

    private final LostItemRepository lostItemRepository;

    public LostItemService(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    public LostItemResponse createLostItem(LostItemCreateRequest request) {
        validateCreateRequest(request);

        LostItem saved = lostItemRepository.save(request.toEntity());
        return LostItemResponse.from(saved);
    }

    public List<LostItemResponse> getLostItems(String keyword, String location) {
        return lostItemRepository.searchLostItems(normalize(keyword), normalize(location))
                .stream()
                .map(LostItemResponse::from)
                .collect(Collectors.toList());
    }

    public Optional<LostItemResponse> getLostItem(Long id) {
        return lostItemRepository.findById(id)
                .map(LostItemResponse::from);
    }

    public UpdateResult updateLostItem(Long id, LostItemUpdateRequest request) {
        validateUpdateRequest(request);

        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return UpdateResult.notFound();
        }

        DeleteResult permissionResult = validatePermission(item, request.getUserId(), request.getPassword());
        if (permissionResult == DeleteResult.INVALID_PASSWORD) {
            return UpdateResult.invalidPassword();
        }

        if (permissionResult == DeleteResult.NOT_WRITER) {
            return UpdateResult.notWriter();
        }

        applyUpdate(item, request);
        LostItem saved = lostItemRepository.save(item);
        return UpdateResult.success(LostItemResponse.from(saved));
    }

    public DeleteResult deleteLostItem(Long id, Map<String, String> payload) {
        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return DeleteResult.NOT_FOUND;
        }

        Map<String, String> request = payload == null ? Collections.emptyMap() : payload;

        DeleteResult permissionResult = validatePermission(item, request.get("userId"), request.get("password"));
        if (permissionResult != DeleteResult.SUCCESS) {
            return permissionResult;
        }

        lostItemRepository.deleteById(id);
        return DeleteResult.SUCCESS;
    }

    private boolean isAnonymous(LostItem item) {
        return ANONYMOUS_WRITER.equals(item.getWriter());
    }

    private boolean isAnonymous(LostItemCreateRequest request) {
        return ANONYMOUS_WRITER.equals(request.getWriter());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private DeleteResult validatePermission(LostItem item, String userId, String password) {
        if (isAnonymous(item)) {
            if (!Objects.equals(item.getPassword(), password)) {
                return DeleteResult.INVALID_PASSWORD;
            }
            return DeleteResult.SUCCESS;
        }

        if (!Objects.equals(item.getWriter(), userId)) {
            return DeleteResult.NOT_WRITER;
        }

        return DeleteResult.SUCCESS;
    }

    private void applyUpdate(LostItem item, LostItemUpdateRequest request) {
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName().trim());
        }

        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }

        if (request.getLocation() != null) {
            item.setLocation(request.getLocation().trim());
        }

        if (request.getLostTime() != null) {
            item.setLostTime(request.getLostTime());
        }
    }

    private void validateCreateRequest(LostItemCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if (isBlank(request.getItemName())) {
            throw new IllegalArgumentException("분실물 이름은 필수입니다.");
        }

        if (isBlank(request.getLocation())) {
            throw new IllegalArgumentException("분실 장소는 필수입니다.");
        }

        if (isBlank(request.getWriter())) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }

        if (isAnonymous(request) && isBlank(request.getPassword())) {
            throw new IllegalArgumentException("익명 글은 비밀번호를 설정해야 합니다.");
        }
    }

    private void validateUpdateRequest(LostItemUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if (request.getItemName() == null
                && request.getDescription() == null
                && request.getLocation() == null
                && request.getLostTime() == null) {
            throw new IllegalArgumentException("수정할 내용이 필요합니다.");
        }

        if (request.getItemName() != null && isBlank(request.getItemName())) {
            throw new IllegalArgumentException("분실물 이름은 비워둘 수 없습니다.");
        }

        if (request.getLocation() != null && isBlank(request.getLocation())) {
            throw new IllegalArgumentException("분실 장소는 비워둘 수 없습니다.");
        }
    }

    public enum DeleteResult {
        SUCCESS("삭제되었습니다."),
        NOT_FOUND(null),
        INVALID_PASSWORD("비밀번호가 틀렸습니다."),
        NOT_WRITER("작성자만 삭제할 수 있습니다.");

        private final String message;

        DeleteResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public record UpdateResult(UpdateStatus status, String message, LostItemResponse item) {

        public static UpdateResult success(LostItemResponse item) {
            return new UpdateResult(UpdateStatus.SUCCESS, "수정되었습니다.", item);
        }

        public static UpdateResult notFound() {
            return new UpdateResult(UpdateStatus.NOT_FOUND, null, null);
        }

        public static UpdateResult invalidPassword() {
            return new UpdateResult(UpdateStatus.INVALID_PASSWORD, "비밀번호가 틀렸습니다.", null);
        }

        public static UpdateResult notWriter() {
            return new UpdateResult(UpdateStatus.NOT_WRITER, "작성자만 수정할 수 있습니다.", null);
        }
    }

    public enum UpdateStatus {
        SUCCESS,
        NOT_FOUND,
        INVALID_PASSWORD,
        NOT_WRITER
    }
}
