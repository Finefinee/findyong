package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.NoticeRequest;
import com.example.lostfound_project.dto.NoticeResponse;
import com.example.lostfound_project.model.Notice;
import com.example.lostfound_project.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    void createNoticeRequiresTitle() {
        NoticeRequest request = new NoticeRequest();
        request.setContent("도서관에서 발견된 물품은 행정실에 보관합니다.");
        request.setWriter("admin");

        assertThatThrownBy(() -> noticeService.createNotice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("공지 제목은 필수입니다.");

        verify(noticeRepository, never()).save(any(Notice.class));
    }

    @Test
    void createNoticeSavesNotice() {
        NoticeRequest request = createRequest("분실물 보관 안내", "행정실에서 찾아가세요.", "admin", "도서관");
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> {
            Notice notice = invocation.getArgument(0);
            notice.setId(1L);
            return notice;
        });

        NoticeResponse response = noticeService.createNotice(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("분실물 보관 안내");
        assertThat(response.targetLocation()).isEqualTo("도서관");
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    void getNoticesReturnsNoticeList() {
        Notice notice = createNotice(1L, "분실물 보관 안내", "행정실에서 찾아가세요.", "admin", "도서관");
        when(noticeRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(notice));

        List<NoticeResponse> notices = noticeService.getNotices();

        assertThat(notices).hasSize(1);
        assertThat(notices.get(0).title()).isEqualTo("분실물 보관 안내");
    }

    @Test
    void getNoticeReturnsEmptyWhenNoticeDoesNotExist() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<NoticeResponse> response = noticeService.getNotice(1L);

        assertThat(response).isEmpty();
    }

    @Test
    void updateNoticeUpdatesExistingNotice() {
        Notice notice = createNotice(1L, "기존 제목", "기존 내용", "admin", "도서관");
        NoticeRequest request = createRequest("수정 제목", "수정 내용", "admin", "학생회관");
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(notice)).thenReturn(notice);

        Optional<NoticeResponse> response = noticeService.updateNotice(1L, request);

        assertThat(response).isPresent();
        assertThat(response.get().title()).isEqualTo("수정 제목");
        assertThat(response.get().targetLocation()).isEqualTo("학생회관");
        verify(noticeRepository).save(notice);
    }

    @Test
    void deleteNoticeReturnsFalseWhenNoticeDoesNotExist() {
        when(noticeRepository.existsById(1L)).thenReturn(false);

        boolean deleted = noticeService.deleteNotice(1L);

        assertThat(deleted).isFalse();
        verify(noticeRepository, never()).deleteById(1L);
    }

    @Test
    void deleteNoticeDeletesExistingNotice() {
        when(noticeRepository.existsById(1L)).thenReturn(true);

        boolean deleted = noticeService.deleteNotice(1L);

        assertThat(deleted).isTrue();
        verify(noticeRepository).deleteById(1L);
    }

    private NoticeRequest createRequest(String title, String content, String writer, String targetLocation) {
        NoticeRequest request = new NoticeRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setWriter(writer);
        request.setTargetLocation(targetLocation);
        return request;
    }

    private Notice createNotice(Long id, String title, String content, String writer, String targetLocation) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setWriter(writer);
        notice.setTargetLocation(targetLocation);
        return notice;
    }
}
