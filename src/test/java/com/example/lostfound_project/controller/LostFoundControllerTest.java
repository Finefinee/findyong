package com.example.lostfound_project.controller;

import com.example.lostfound_project.dto.LostItemResponse;
import com.example.lostfound_project.service.LostItemService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LostFoundController.class)
@AutoConfigureMockMvc(addFilters = false)
class LostFoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LostItemService lostItemService;

    @Test
    void createLostItemDoesNotExposePassword() throws Exception {
        LostItemResponse response = new LostItemResponse(
                1L,
                "지갑",
                "검은색 지갑",
                "도서관",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                "익명"
        );
        when(lostItemService.createLostItem(any())).thenReturn(response);

        mockMvc.perform(post("/api/lost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemName": "지갑",
                                  "description": "검은색 지갑",
                                  "location": "도서관",
                                  "lostTime": "2026-06-24T10:00:00",
                                  "writer": "익명",
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("지갑"))
                .andExpect(jsonPath("$.writer").value("익명"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getLostItemsReturnsSearchResultWithoutPassword() throws Exception {
        LostItemResponse response = new LostItemResponse(
                1L,
                "지갑",
                "검은색 지갑",
                "도서관",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                "익명"
        );
        when(lostItemService.getLostItems(eq("지갑"), eq("도서관"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/lost")
                        .param("keyword", "지갑")
                        .param("location", "도서관"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName").value("지갑"))
                .andExpect(jsonPath("$[0].location").value("도서관"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getLostItemReturnsDetailWithoutPassword() throws Exception {
        LostItemResponse response = new LostItemResponse(
                1L,
                "지갑",
                "검은색 지갑",
                "도서관",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                "익명"
        );
        when(lostItemService.getLostItem(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/lost/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.itemName").value("지갑"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateLostItemReturnsUpdatedItemWithoutPassword() throws Exception {
        LostItemResponse response = new LostItemResponse(
                1L,
                "수정된 지갑",
                "검은색 지갑",
                "학생회관",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                "user1"
        );
        when(lostItemService.updateLostItem(eq(1L), any()))
                .thenReturn(LostItemService.UpdateResult.success(response));

        mockMvc.perform(patch("/api/lost/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemName": "수정된 지갑",
                                  "location": "학생회관",
                                  "userId": "user1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("수정된 지갑"))
                .andExpect(jsonPath("$.location").value("학생회관"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
