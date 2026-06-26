package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.LostItemCreateRequest;
import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.dto.LostItemUpdateRequest;
import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LostItemService {

    private final LostItemRepository lostItemRepository;

    public LostItemService(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    public LostItemResponse createLostItem(LostItemCreateRequest request, String userId) {
        validateCreateRequest(request);

        LostItem saved = lostItemRepository.save(request.toEntity(userId));
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

    public UpdateResult updateLostItem(Long id, LostItemUpdateRequest request, String userId) {
        validateUpdateRequest(request);

        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return UpdateResult.notFound();
        }

        if (!isWriter(item, userId)) {
            return UpdateResult.notWriter();
        }

        applyUpdate(item, request);
        LostItem saved = lostItemRepository.save(item);
        return UpdateResult.success(LostItemResponse.from(saved));
    }

    public DeleteResult deleteLostItem(Long id, String userId) {
        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return DeleteResult.NOT_FOUND;
        }

        if (!isWriter(item, userId)) {
            return DeleteResult.NOT_WRITER;
        }

        lostItemRepository.deleteById(id);
        return DeleteResult.SUCCESS;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isWriter(LostItem item, String userId) {
        return Objects.equals(item.getWriter(), userId);
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

        public static UpdateResult notWriter() {
            return new UpdateResult(UpdateStatus.NOT_WRITER, "작성자만 수정할 수 있습니다.", null);
        }
    }

    public enum UpdateStatus {
        SUCCESS,
        NOT_FOUND,
        NOT_WRITER
    }
}
