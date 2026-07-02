package com.codeit.server.ai.client;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

  private static final String SYSTEM_INSTRUCTION = """
    <role>
    너는 입력된 뉴스 기사 본문 텍스트를 정확하게 분석하고 요약하는 '뉴스 요약 및 키워드 추출 자동화 시스템'이야.
    </role>
    
    <core_directive>
    1. 기사의 핵심 주장을 왜곡하거나 사실을 꾸며내지(Hallucination) 말고, 본문에 기재된 정보에만 기반하여 요약해.
    2. 기사의 논조를 중립적으로 유지해.
    3. 출력은 반드시 지정된 JSON 스키마를 엄격히 준수하여 순수 JSON 포맷으로만 반환해.
    </core_directive>
    """;

  private static final String USER_PROMPT_TEMPLATE = """
    <task>
    제공된 뉴스 기사 본문을 분석하여 핵심 내용을 요약하고 관련 키워드를 추출해.
    </task>
    
    <rules>
    1. 요약(summary): 기사의 가장 중요한 핵심 내용을 3개 이내의 완성형 문장으로 한국어로 작성하되, 가독성을 위해 각 문장은 줄바꿈(\\n)으로 구분하여 하나의 문자열로 결합하여 제공해.
    2. 키워드(keywords): 기사를 대표하는 고유 명사나 핵심 단어 3~5개를 추출하라. (예: ["충남대", "바이오 R&D", "국가연구소"])
    3. 광고 문구, 기자 이메일, 무관한 안내문구 등 기사 본문과 상관없는 노이즈는 요약 및 키워드에서 완전히 제외하라.
    </rules>
    
    <article_content>
    %s
    </article_content>
    """;

  private static final String MODEL = "gemini-2.5-flash-lite";

  private final ObjectMapper objectMapper;
  private final Client geminiClient;

  public NewsSummaryResponseDto summarizeNews(String articleContent) {
    long start = System.nanoTime();
    
    GenerateContentResponse response = geminiClient.models
        .generateContent(MODEL, buildContent(articleContent), buildConfig());
        
    long elapsed = (System.nanoTime() - start) / 1_000_000;
    log.info("Gemini News 요약 응답 완료 - 소요 시간: {}ms", elapsed);
    
    return parseResponse(response);
  }

  private GenerateContentConfig buildConfig() {
    Schema schema = Schema.builder()
        .type(Type.Known.OBJECT)
        .properties(ImmutableMap.of(
            "summary", Schema.builder()
                .type(Type.Known.STRING)
                .description("기사 내용을 한국어로 3줄 요약한 결과 (문장 간 \\n 구분)")
                .build(),
            "keywords", Schema.builder()
                .type(Type.Known.ARRAY)
                .items(Schema.builder().type(Type.Known.STRING).build())
                .description("기사를 대표하는 핵심 키워드 3~5개 리스트")
                .build()
        ))
        .required(ImmutableList.of("summary", "keywords"))
        .build();

    return GenerateContentConfig.builder()
        .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
        .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
        .temperature(0.0f)
        .responseMimeType("application/json")
        .candidateCount(1)
        .responseSchema(schema)
        .maxOutputTokens(512)
        .build();
  }

  private Content buildContent(String articleContent) {
    String userPrompt = String.format(USER_PROMPT_TEMPLATE, articleContent);
    return Content.fromParts(Part.fromText(userPrompt));
  }

  private NewsSummaryResponseDto parseResponse(GenerateContentResponse response) {
    try {
      String text = response.text();
      if (text == null || text.isBlank()) {
        throw new RuntimeException("Gemini 응답이 비어 있습니다.");
      }
      
      NewsSummaryResponseDto summaryResponse = objectMapper.readValue(text, NewsSummaryResponseDto.class);
      log.info("Gemini 뉴스 요약 성공 - Keywords: {}", summaryResponse.getKeywords());
      return summaryResponse;
      
    } catch (JsonProcessingException e) {
      log.error("Gemini 응답 파싱 실패", e);
      throw new RuntimeException("요약 데이터 파싱 중 오류가 발생했습니다.", e);
    }
  }
}
