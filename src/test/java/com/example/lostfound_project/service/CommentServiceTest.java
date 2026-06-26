package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.CommentCreateRequest;
import com.example.lostfound_project.dto.CommentResponse;
import com.example.lostfound_project.model.Comment;
import com.example.lostfound_project.repository.CommentRepository;
import com.example.lostfound_project.repository.LostItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LostItemRepository lostItemRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentsReturnsResponses() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setItemId(10L);
        comment.setContent("확인했습니다.");
        comment.setWriter("user1");
        comment.setCreatedAt(LocalDateTime.of(2026, 6, 26, 12, 0));
        when(commentRepository.findByItemIdOrderByCreatedAtAscIdAsc(10L)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getComments(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("확인했습니다.");
        assertThat(responses.get(0).writer()).isEqualTo("user1");
    }

    @Test
    void createCommentRequiresContent() {
        CommentCreateRequest request = new CommentCreateRequest();

        assertThatThrownBy(() -> commentService.createComment(10L, request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수입니다.");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createCommentReturnsNotFoundWhenLostItemDoesNotExist() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("확인했습니다.");
        when(lostItemRepository.existsById(10L)).thenReturn(false);

        CommentService.CreateResult result = commentService.createComment(10L, request, "user1");

        assertThat(result.status()).isEqualTo(CommentService.CreateStatus.NOT_FOUND);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createCommentSavesValidComment() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent(" 확인했습니다. ");
        when(lostItemRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            return comment;
        });

        CommentService.CreateResult result = commentService.createComment(10L, request, "user1");

        assertThat(result.status()).isEqualTo(CommentService.CreateStatus.SUCCESS);
        assertThat(result.comment().itemId()).isEqualTo(10L);
        assertThat(result.comment().content()).isEqualTo("확인했습니다.");
        assertThat(result.comment().writer()).isEqualTo("user1");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteCommentRejectsWrongWriter() {
        Comment comment = new Comment();
        comment.setItemId(10L);
        comment.setWriter("user1");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        CommentService.DeleteResult result = commentService.deleteComment(10L, 1L, "user2");

        assertThat(result).isEqualTo(CommentService.DeleteResult.NOT_WRITER);
        verify(commentRepository, never()).deleteById(1L);
    }

    @Test
    void deleteCommentDeletesWhenWriterMatches() {
        Comment comment = new Comment();
        comment.setItemId(10L);
        comment.setWriter("user1");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        CommentService.DeleteResult result = commentService.deleteComment(10L, 1L, "user1");

        assertThat(result).isEqualTo(CommentService.DeleteResult.SUCCESS);
        verify(commentRepository).deleteById(1L);
    }
}
