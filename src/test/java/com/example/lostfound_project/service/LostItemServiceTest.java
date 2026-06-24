package com.example.lostfound_project.service;

import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.repository.LostItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LostItemServiceTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @InjectMocks
    private LostItemService lostItemService;

    @Test
    void anonymousLostItemRequiresPassword() {
        LostItem item = new LostItem();
        item.setWriter("익명");

        assertThatThrownBy(() -> lostItemService.createLostItem(item))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("익명 글은 비밀번호를 설정해야 합니다.");

        verify(lostItemRepository, never()).save(item);
    }

    @Test
    void createLostItemSavesValidItem() {
        LostItem item = new LostItem();
        item.setWriter("user1");
        when(lostItemRepository.save(item)).thenReturn(item);

        LostItem saved = lostItemService.createLostItem(item);

        assertThat(saved).isSameAs(item);
        verify(lostItemRepository).save(item);
    }

    @Test
    void deleteLostItemReturnsNotFoundWhenItemDoesNotExist() {
        when(lostItemRepository.findById(1L)).thenReturn(Optional.empty());

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, Map.of());

        assertThat(result).isEqualTo(LostItemService.DeleteResult.NOT_FOUND);
        verify(lostItemRepository, never()).deleteById(1L);
    }

    @Test
    void deleteAnonymousLostItemRejectsWrongPassword() {
        LostItem item = new LostItem();
        item.setWriter("익명");
        item.setPassword("1234");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, Map.of("password", "0000"));

        assertThat(result).isEqualTo(LostItemService.DeleteResult.INVALID_PASSWORD);
        verify(lostItemRepository, never()).deleteById(1L);
    }

    @Test
    void deleteLostItemDeletesWhenWriterMatches() {
        LostItem item = new LostItem();
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, Map.of("userId", "user1"));

        assertThat(result).isEqualTo(LostItemService.DeleteResult.SUCCESS);
        verify(lostItemRepository).deleteById(1L);
    }
}
