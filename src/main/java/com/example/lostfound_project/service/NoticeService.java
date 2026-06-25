package com.example.lostfound_project.service;

import com.example.lostfound_project.dto.NoticeRequest;
import com.example.lostfound_project.dto.NoticeResponse;
import com.example.lostfound_project.model.Notice;
import com.example.lostfound_project.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public NoticeResponse createNotice(NoticeRequest request) {
        validateRequest(request);

        Notice saved = noticeRepository.save(request.toEntity());
        return NoticeResponse.from(saved);
    }

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    public Optional<NoticeResponse> getNotice(Long id) {
        return noticeRepository.findById(id)
                .map(NoticeResponse::from);
    }

    public Optional<NoticeResponse> updateNotice(Long id, NoticeRequest request) {
        validateRequest(request);

        return noticeRepository.findById(id)
                .map(notice -> {
                    notice.setTitle(request.getTitle());
                    notice.setContent(request.getContent());
                    notice.setWriter(request.getWriter());
                    notice.setTargetLocation(request.getTargetLocation());
                    return NoticeResponse.from(noticeRepository.save(notice));
                });
    }

    public boolean deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            return false;
        }

        noticeRepository.deleteById(id);
        return true;
    }

    private void validateRequest(NoticeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException("공지 제목은 필수입니다.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("공지 내용은 필수입니다.");
        }

        if (isBlank(request.getWriter())) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
