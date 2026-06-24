package com.example.lostfound_project.service;

import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LostItemService {

    private static final String ANONYMOUS_WRITER = "익명";

    private final LostItemRepository lostItemRepository;

    public LostItemService(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    public LostItem createLostItem(LostItem item) {
        if (isAnonymous(item) && isBlank(item.getPassword())) {
            throw new IllegalArgumentException("익명 글은 비밀번호를 설정해야 합니다.");
        }

        return lostItemRepository.save(item);
    }

    public List<LostItem> getAllLostItems() {
        return lostItemRepository.findAll();
    }

    public DeleteResult deleteLostItem(Long id, Map<String, String> payload) {
        LostItem item = lostItemRepository.findById(id).orElse(null);
        if (item == null) {
            return DeleteResult.NOT_FOUND;
        }

        Map<String, String> request = payload == null ? Collections.emptyMap() : payload;

        if (isAnonymous(item)) {
            String password = request.get("password");
            if (!Objects.equals(item.getPassword(), password)) {
                return DeleteResult.INVALID_PASSWORD;
            }
        } else {
            String userId = request.get("userId");
            if (!item.getWriter().equals(userId)) {
                return DeleteResult.NOT_WRITER;
            }
        }

        lostItemRepository.deleteById(id);
        return DeleteResult.SUCCESS;
    }

    private boolean isAnonymous(LostItem item) {
        return ANONYMOUS_WRITER.equals(item.getWriter());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
}
