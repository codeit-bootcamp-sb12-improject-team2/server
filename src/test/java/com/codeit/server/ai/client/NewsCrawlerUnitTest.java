package com.codeit.server.ai.client;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NewsCrawlerUnitTest {

    private NewsCrawler newsCrawler;
    private MockedStatic<Jsoup> jsoupMock;

    @BeforeEach
    void setUp() {
        newsCrawler = new NewsCrawler();
        jsoupMock = mockStatic(Jsoup.class);
    }

    @AfterEach
    void tearDown() {
        jsoupMock.close();
    }

    private Connection mockJsoupConnection(Document mockDocument) throws IOException {
        Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.header(anyString(), anyString())).thenReturn(connection);
        when(connection.maxBodySize(anyInt())).thenReturn(connection);
        when(connection.followRedirects(anyBoolean())).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.get()).thenReturn(mockDocument);
        return connection;
    }

    @Test
    void crawl_whenChosunNewsWithScript_shouldExtractContent() throws Exception {
        // Given
        String url = "https://www.chosun.com/national/2026/07/06/test/";
        Document mockDocument = mock(Document.class);
        Connection mockConnection = mockJsoupConnection(mockDocument);
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);

        // chosun.com script parsing mock
        Element mockScript = mock(Element.class);
        when(mockScript.data()).thenReturn("Fusion.globalContent= { \"content\": \"조선일보 본문 내용입니다.\" }");
        when(mockDocument.selectFirst("script#fusion-metadata")).thenReturn(mockScript);

        // When
        String result = newsCrawler.crawl(url);

        // Then
        assertThat(result).isEqualTo("조선일보 본문 내용입니다.");
    }

    @Test
    void crawl_whenGeneralNewsWithSelector_shouldExtractContent() throws Exception {
        // Given
        String url = "https://news.naver.com/main/read.nhn";
        Document mockDocument = mock(Document.class);
        Connection mockConnection = mockJsoupConnection(mockDocument);
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);

        // chosun script는 없음
        when(mockDocument.selectFirst("script#fusion-metadata")).thenReturn(null);

        // 일반 셀렉터 (#dic_area) 가 동작하도록 설정
        Elements mockElements = mock(Elements.class);
        Element mockElement = mock(Element.class);
        
        when(mockDocument.select("#dic_area")).thenReturn(mockElements);
        when(mockElements.isEmpty()).thenReturn(false);
        when(mockElements.first()).thenReturn(mockElement);
        when(mockElement.text()).thenReturn("네이버 뉴스 본문 내용입니다.");

        // When
        String result = newsCrawler.crawl(url);

        // Then
        assertThat(result).isEqualTo("네이버 뉴스 본문 내용입니다.");
    }

    @Test
    void crawl_whenGeneralNewsWithPTags_shouldExtractContent() throws Exception {
        // Given
        String url = "https://some-other-news.com/post/1";
        Document mockDocument = mock(Document.class);
        Connection mockConnection = mockJsoupConnection(mockDocument);
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);

        when(mockDocument.selectFirst("script#fusion-metadata")).thenReturn(null);

        // 모든 일반 셀렉터 결과가 비어있음
        String[] selectors = {"#dic_area", ".article-view-content-div", ".article-body", "#article-body", "#articleBody", ".article_body", ".article_content", "article", "#articleBodyContents"};
        for (String selector : selectors) {
            Elements emptyElements = mock(Elements.class);
            when(emptyElements.isEmpty()).thenReturn(true);
            when(mockDocument.select(selector)).thenReturn(emptyElements);
        }

        // p 태그가 글을 가지고 있는 경우
        Elements pElements = mock(Elements.class);
        when(pElements.isEmpty()).thenReturn(false);
        when(pElements.text()).thenReturn("p태그의 본문입니다.");
        when(mockDocument.select("p")).thenReturn(pElements);

        // When
        String result = newsCrawler.crawl(url);

        // Then
        assertThat(result).isEqualTo("p태그의 본문입니다.");
    }

    @Test
    void crawl_whenNoContentExtracted_shouldThrowRuntimeException() throws Exception {
        // Given
        String url = "https://some-other-news.com/post/1";
        Document mockDocument = mock(Document.class);
        Connection mockConnection = mockJsoupConnection(mockDocument);
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);

        when(mockDocument.selectFirst("script#fusion-metadata")).thenReturn(null);

        // 모든 셀렉터 및 p 태그가 비어있음
        String[] selectors = {"#dic_area", ".article-view-content-div", ".article-body", "#article-body", "#articleBody", ".article_body", ".article_content", "article", "#articleBodyContents", "p"};
        for (String selector : selectors) {
            Elements emptyElements = mock(Elements.class);
            when(emptyElements.isEmpty()).thenReturn(true);
            when(mockDocument.select(selector)).thenReturn(emptyElements);
        }

        // When & Then
        assertThatThrownBy(() -> newsCrawler.crawl(url))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("기사 본문 추출 실패");
    }

    @Test
    void crawl_whenConnectionFails_shouldThrowRuntimeException() throws Exception {
        // Given
        String url = "https://failed-news.com";
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
        when(mockConnection.header(anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.maxBodySize(anyInt())).thenReturn(mockConnection);
        when(mockConnection.followRedirects(anyBoolean())).thenReturn(mockConnection);
        when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
        
        // Jsoup.get() 에서 IOException 발생
        when(mockConnection.get()).thenThrow(new IOException("Timeout"));
        jsoupMock.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);

        // When & Then
        assertThatThrownBy(() -> newsCrawler.crawl(url))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("기사 크롤링 실패");
    }
}
