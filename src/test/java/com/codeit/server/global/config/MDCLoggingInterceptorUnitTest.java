package com.codeit.server.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MDCLoggingInterceptorUnitTest {

    private MDCLoggingInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new MDCLoggingInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void preHandle_shouldPutMdcPropertiesAndAddResponseHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertThat(result).isTrue();

        // MDC에 값들이 정상 등록되었는지 검증
        assertThat(MDC.get(MDCLoggingInterceptor.REQUEST_METHOD)).isEqualTo("GET");
        assertThat(MDC.get(MDCLoggingInterceptor.REQUEST_URI)).isEqualTo("/api/test");
        assertThat(MDC.get(MDCLoggingInterceptor.REQUEST_ID)).contains("1.2.3.4");

        // 응답 헤더에 요청 ID가 세팅되었는지 확인
        verify(response).setHeader(eq(MDCLoggingInterceptor.REQUEST_ID_HEADER), anyString());
    }

    @Test
    void afterCompletion_shouldClearMdc() throws Exception {
        // Given
        MDC.put(MDCLoggingInterceptor.REQUEST_ID, "test-id");

        // When
        interceptor.afterCompletion(request, response, new Object(), null);

        // Then
        // MDC가 깔끔히 비워졌는지 검증
        assertThat(MDC.get(MDCLoggingInterceptor.REQUEST_ID)).isNull();
    }
}
