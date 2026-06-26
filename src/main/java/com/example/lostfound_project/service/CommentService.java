package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.CommentCreateRequest;
import com.example.lostfound_project.dto.CommentResponse;
import com.example.lostfound_project.model.Comment;
import com.example.lostfound_project.repository.CommentRepository;
import com.example.lostfound_project.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final LostItemRepository lostItemRepository;

    public CommentService(CommentRepository commentRepository, LostItemRepository lostItemRepository) {
        this.commentRepository = commentRepository;
        this.lostItemRepository = lostItemRepository;
    }

    public List<CommentResponse> getComments(Long itemId) {
        return commentRepository.findByItemIdOrderByCreatedAtAscIdAsc(itemId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CreateResult createComment(Long itemId, CommentCreateRequest request, String userId) {
        validateCreateRequest(request);

        if (!lostItemRepository.existsById(itemId)) {
            return CreateResult.notFound();
        }

        Comment comment = new Comment();
        comment.setItemId(itemId);
        comment.setContent(request.getContent().trim());
        comment.setWriter(userId);
        comment.setCreatedAt(LocalDateTime.now());

        return CreateResult.success(CommentResponse.from(commentRepository.save(comment)));
    }

    public DeleteResult deleteComment(Long itemId, Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || !Objects.equals(comment.getItemId(), itemId)) {
            return DeleteResult.NOT_FOUND;
        }

        if (!Objects.equals(comment.getWriter(), userId)) {
            return DeleteResult.NOT_WRITER;
        }

        commentRepository.deleteById(commentId);
        return DeleteResult.SUCCESS;
    }

    private void validateCreateRequest(CommentCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }
    }

    public record CreateResult(CreateStatus status, String message, CommentResponse comment) {

        public static CreateResult success(CommentResponse comment) {
            return new CreateResult(CreateStatus.SUCCESS, "댓글이 등록되었습니다.", comment);
        }

        public static CreateResult notFound() {
            return new CreateResult(CreateStatus.NOT_FOUND, null, null);
        }
    }

    public enum CreateStatus {
        SUCCESS,
        NOT_FOUND
    }

    public enum DeleteResult {
        SUCCESS("댓글이 삭제되었습니다."),
        NOT_FOUND(null),
        NOT_WRITER("작성자만 댓글을 삭제할 수 있습니다.");

        private final String message;

        DeleteResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
