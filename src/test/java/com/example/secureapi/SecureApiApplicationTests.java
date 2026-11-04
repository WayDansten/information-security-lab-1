package com.example.secureapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.secureapi.auth.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecureApiApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedDataRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/data")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndProtectedPostWorkAndHtmlIsRemoved() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "student", "password", "student-password"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readValue(response, TokenResponse.class).accessToken();

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "<b>safe title</b>",
                                "content", "<script>alert('xss')</script>plain text"))))
                .andExpect(status().isCreated())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("plain text")));
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "student", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }
}