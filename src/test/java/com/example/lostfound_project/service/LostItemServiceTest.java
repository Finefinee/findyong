package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.LostItemCreateRequest;
import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.dto.LostItemStatusUpdateRequest;
import com.example.lostfound_project.dto.LostItemUpdateRequest;
import com.example.lostfound_project.model.LostItem;
import com.example.lostfound_project.model.LostItemStatus;
import com.example.lostfound_project.repository.LostItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    void createLostItemRequiresLocation() {
        LostItemCreateRequest request = new LostItemCreateRequest();
        request.setItemName("지갑");

        assertThatThrownBy(() -> lostItemService.createLostItem(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("분실 장소는 필수입니다.");

        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void createLostItemRequiresItemName() {
        LostItemCreateRequest request = new LostItemCreateRequest();
        request.setLocation("도서관");

        assertThatThrownBy(() -> lostItemService.createLostItem(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("분실물 이름은 필수입니다.");

        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void createLostItemSavesValidItem() {
        LocalDateTime lostTime = LocalDateTime.of(2026, 6, 24, 10, 0);
        LostItemCreateRequest request = new LostItemCreateRequest();
        request.setItemName("지갑");
        request.setDescription("검은색 지갑");
        request.setLocation("도서관");
        request.setLostTime(lostTime);

        when(lostItemRepository.save(any(LostItem.class))).thenAnswer(invocation -> {
            LostItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        LostItemResponse saved = lostItemService.createLostItem(request, "user1");

        assertThat(saved.id()).isEqualTo(1L);
        assertThat(saved.itemName()).isEqualTo("지갑");
        assertThat(saved.description()).isEqualTo("검은색 지갑");
        assertThat(saved.location()).isEqualTo("도서관");
        assertThat(saved.lostTime()).isEqualTo(lostTime);
        assertThat(saved.writer()).isEqualTo("user1");
        assertThat(saved.status()).isEqualTo(LostItemStatus.OWNER_NOT_FOUND);
        verify(lostItemRepository).save(any(LostItem.class));
    }

    @Test
    void lostItemResponseDoesNotExposePassword() {
        List<String> fields = Arrays.stream(LostItemResponse.class.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();

        assertThat(fields).doesNotContain("password");
    }

    @Test
    void getLostItemsSearchesAndReturnsResponses() {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setItemName("지갑");
        item.setDescription("검은색 지갑");
        item.setLocation("도서관");
        item.setWriter("user1");
        when(lostItemRepository.searchLostItems("지갑", "도서관")).thenReturn(List.of(item));

        List<LostItemResponse> responses = lostItemService.getLostItems(" 지갑 ", " 도서관 ");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).itemName()).isEqualTo("지갑");
        assertThat(responses.get(0).status()).isEqualTo(LostItemStatus.OWNER_NOT_FOUND);
        verify(lostItemRepository).searchLostItems("지갑", "도서관");
    }

    @Test
    void getLostItemReturnsResponseWhenItemExists() {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setItemName("지갑");
        item.setLocation("도서관");
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<LostItemResponse> response = lostItemService.getLostItem(1L);

        assertThat(response).isPresent();
        assertThat(response.get().id()).isEqualTo(1L);
        assertThat(response.get().itemName()).isEqualTo("지갑");
        assertThat(response.get().status()).isEqualTo(LostItemStatus.OWNER_NOT_FOUND);
    }

    @Test
    void updateLostItemRequiresUpdateContent() {
        LostItemUpdateRequest request = new LostItemUpdateRequest();

        assertThatThrownBy(() -> lostItemService.updateLostItem(1L, request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정할 내용이 필요합니다.");

        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void updateLostItemRejectsWrongWriter() {
        LostItem item = new LostItem();
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        LostItemUpdateRequest request = new LostItemUpdateRequest();
        request.setItemName("수정된 지갑");

        LostItemService.UpdateResult result = lostItemService.updateLostItem(1L, request, "user2");

        assertThat(result.status()).isEqualTo(LostItemService.UpdateStatus.NOT_WRITER);
        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void updateLostItemUpdatesAllowedFields() {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setItemName("지갑");
        item.setLocation("도서관");
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(lostItemRepository.save(item)).thenReturn(item);

        LostItemUpdateRequest request = new LostItemUpdateRequest();
        request.setItemName("수정된 지갑");
        request.setLocation("학생회관");

        LostItemService.UpdateResult result = lostItemService.updateLostItem(1L, request, "user1");

        assertThat(result.status()).isEqualTo(LostItemService.UpdateStatus.SUCCESS);
        assertThat(result.item().itemName()).isEqualTo("수정된 지갑");
        assertThat(result.item().location()).isEqualTo("학생회관");
        verify(lostItemRepository).save(item);
    }

    @Test
    void updateLostItemStatusRequiresStatus() {
        LostItemStatusUpdateRequest request = new LostItemStatusUpdateRequest();

        assertThatThrownBy(() -> lostItemService.updateLostItemStatus(1L, request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("분실물 상태는 필수입니다.");

        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void updateLostItemStatusUpdatesStatusWhenWriterMatches() {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setItemName("지갑");
        item.setLocation("도서관");
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(lostItemRepository.save(item)).thenReturn(item);

        LostItemStatusUpdateRequest request = new LostItemStatusUpdateRequest();
        request.setStatus(LostItemStatus.OWNER_FOUND);

        LostItemService.UpdateResult result = lostItemService.updateLostItemStatus(1L, request, "user1");

        assertThat(result.status()).isEqualTo(LostItemService.UpdateStatus.SUCCESS);
        assertThat(result.item().status()).isEqualTo(LostItemStatus.OWNER_FOUND);
        verify(lostItemRepository).save(item);
    }

    @Test
    void deleteLostItemReturnsNotFoundWhenItemDoesNotExist() {
        when(lostItemRepository.findById(1L)).thenReturn(Optional.empty());

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, "user1");

        assertThat(result).isEqualTo(LostItemService.DeleteResult.NOT_FOUND);
        verify(lostItemRepository, never()).deleteById(1L);
    }

    @Test
    void deleteLostItemRejectsWrongWriter() {
        LostItem item = new LostItem();
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, "user2");

        assertThat(result).isEqualTo(LostItemService.DeleteResult.NOT_WRITER);
        verify(lostItemRepository, never()).deleteById(1L);
    }

    @Test
    void deleteLostItemDeletesWhenWriterMatches() {
        LostItem item = new LostItem();
        item.setWriter("user1");
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(item));

        LostItemService.DeleteResult result = lostItemService.deleteLostItem(1L, "user1");

        assertThat(result).isEqualTo(LostItemService.DeleteResult.SUCCESS);
        verify(lostItemRepository).deleteById(1L);
    }
}
