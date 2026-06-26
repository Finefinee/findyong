package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.NoticeResponse;
import com.example.lostfound_project.security.JwtAuthenticationFilter;
import com.example.lostfound_project.service.NoticeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoticeService noticeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createNoticeReturnsCreatedNotice() throws Exception {
        NoticeResponse response = createResponse("분실물 보관 안내", "행정실에서 찾아가세요.", "admin", "도서관");
        when(noticeService.createNotice(any())).thenReturn(response);

        mockMvc.perform(post("/api/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "분실물 보관 안내",
                                  "content": "행정실에서 찾아가세요.",
                                  "writer": "admin",
                                  "targetLocation": "도서관"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("분실물 보관 안내"))
                .andExpect(jsonPath("$.targetLocation").value("도서관"));
    }

    @Test
    void getNoticesReturnsNoticeList() throws Exception {
        NoticeResponse response = createResponse("분실물 보관 안내", "행정실에서 찾아가세요.", "admin", "도서관");
        when(noticeService.getNotices()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("분실물 보관 안내"));
    }

    @Test
    void getNoticeReturnsNoticeDetail() throws Exception {
        NoticeResponse response = createResponse("분실물 보관 안내", "행정실에서 찾아가세요.", "admin", "도서관");
        when(noticeService.getNotice(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/notices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("분실물 보관 안내"));
    }

    @Test
    void updateNoticeReturnsUpdatedNotice() throws Exception {
        NoticeResponse response = createResponse("수정된 공지", "학생회관에서 찾아가세요.", "admin", "학생회관");
        when(noticeService.updateNotice(eq(1L), any())).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/notices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정된 공지",
                                  "content": "학생회관에서 찾아가세요.",
                                  "writer": "admin",
                                  "targetLocation": "학생회관"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 공지"))
                .andExpect(jsonPath("$.targetLocation").value("학생회관"));
    }

    @Test
    void deleteNoticeReturnsMessage() throws Exception {
        when(noticeService.deleteNotice(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }

    private NoticeResponse createResponse(String title, String content, String writer, String targetLocation) {
        return new NoticeResponse(
                1L,
                title,
                content,
                writer,
                targetLocation,
                LocalDateTime.of(2026, 6, 24, 10, 0),
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
    }
}
