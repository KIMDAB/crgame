package com.toy.cnr.api.userconsent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserConsentApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final String BASE_URL = "/v1/user-consent";

    // ── 샘플 요청 JSON ──────────────────────────────────────────────────────────

    private String createRequest() {
        return """
                {
                    "consentItem": "test-value",
                    "agreed": true,
                    "lastModifiedDate": "2026-01-01T10:00:00",
                    "deviceId": "test-value",
                    "userId": 100
                }
                """;
    }

    private String updateRequest() {
        return """
                {
                    "consentItem": "updated-value",
                    "agreed": true,
                    "lastModifiedDate": "2026-06-01T10:00:00",
                    "deviceId": "updated-value",
                    "userId": 200
                }
                """;
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────────────────

    private long createSample() throws Exception {
        var response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    // ── 테스트 케이스 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: 데이터 없을 때 200 빈 배열 반환")
    void findAll_empty_returns200WithEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("findAll: 데이터 있을 때 200 배열 반환")
    void findAll_withData_returns200WithItems() throws Exception {
        createSample();
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("findById: 존재하는 id 조회 시 200 반환")
    void findById_existingId_returns200() throws Exception {
        long id = createSample();
        mockMvc.perform(get(BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("findById: 존재하지 않는 id 조회 시 404 반환")
    void findById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorType").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("create: 유효한 요청 시 200 및 생성된 데이터 반환")
    void create_validRequest_returns200WithCreatedData() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.consentItem").value("test-value"))
            .andExpect(jsonPath("$.agreed").value(true))
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andExpect(jsonPath("$.deviceId").value("test-value"))
            .andExpect(jsonPath("$.userId").value(100));
    }

    @Test
    @DisplayName("update: 존재하는 id 수정 시 200 및 수정된 데이터 반환")
    void update_existingId_returns200WithUpdatedData() throws Exception {
        long id = createSample();

        mockMvc.perform(put(BASE_URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.consentItem").value("updated-value"))
            .andExpect(jsonPath("$.agreed").value(true))
            .andExpect(jsonPath("$.lastModifiedDate").exists())
            .andExpect(jsonPath("$.deviceId").value("updated-value"))
            .andExpect(jsonPath("$.userId").value(200));
    }

    @Test
    @DisplayName("update: 존재하지 않는 id 수정 시 404 반환")
    void update_nonExistingId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorType").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("delete: 존재하는 id 삭제 시 204 반환")
    void delete_existingId_returns204() throws Exception {
        long id = createSample();
        mockMvc.perform(delete(BASE_URL + "/" + id))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete: 존재하지 않는 id 삭제 시 204 반환 (멱등성)")
    void delete_nonExistingId_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999"))
            .andExpect(status().isNoContent());
    }
}
