package com.codeit.server.ai.client;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiClientUnitTest {

    @Mock
    private Client mockGenaiClient;

    @Mock
    private Models mockModels;

    private ObjectMapper objectMapper;
    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        // Client.models (public final 필드) 에 mockModels 주입
        Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        modelsField.set(mockGenaiClient, mockModels);

        geminiClient = new GeminiClient(objectMapper, mockGenaiClient);
    }

    @Test
    void summarizeNews_whenSuccess_shouldReturnNewsSummaryResponseDto() {
        // Given
        String articleContent = "이것은 뉴스 본문 테스트 내용입니다.";
        String mockJsonResponse = "{\"summary\":\"요약 내용\",\"keywords\":[\"키1\",\"키2\"]}";

        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn(mockJsonResponse);

        when(mockModels.generateContent(
                eq("gemini-2.5-flash"),
                any(Content.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // When
        NewsSummaryResponseDto actualDto = geminiClient.summarizeNews(articleContent);

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getSummary()).isEqualTo("요약 내용");
        assertThat(actualDto.getKeywords()).containsExactly("키1", "키2");
    }

    @Test
    void summarizeNews_whenResponseEmpty_shouldThrowRuntimeException() {
        // Given
        String articleContent = "이것은 뉴스 본문 테스트 내용입니다.";

        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn(""); // 비어있는 텍스트

        when(mockModels.generateContent(
                eq("gemini-2.5-flash"),
                any(Content.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> geminiClient.summarizeNews(articleContent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini 응답이 비어 있습니다.");
    }

    @Test
    void summarizeNews_whenJsonParsingFailed_shouldThrowRuntimeException() {
        // Given
        String articleContent = "이것은 뉴스 본문 테스트 내용입니다.";
        String invalidJson = "{invalid-json-format}";

        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn(invalidJson);

        when(mockModels.generateContent(
                eq("gemini-2.5-flash"),
                any(Content.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> geminiClient.summarizeNews(articleContent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("요약 데이터 파싱 중 오류가 발생했습니다.");
    }
}
