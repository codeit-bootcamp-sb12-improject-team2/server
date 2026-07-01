package com.codeit.server.comment.service;


import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommentModerationClient {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${openai.api-key:}")
  private String apiKey;

  public boolean isFlagged(String content) {
    try {
      if (!StringUtils.hasText(apiKey)) {
        log.error("OpenAI API key is missing. Check OPENAI_API_KEY in the runtime environment.");
        throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED);
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(apiKey);
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = Map.of(
          "model", "omni-moderation-latest",
          "input", content
      );

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

      log.info("Sending moderation request to OpenAI");
      String responseBody = restTemplate.exchange(
          "https://api.openai.com/v1/moderations",
          HttpMethod.POST,
          entity,
          String.class
      ).getBody();

      log.info("OpenAI moderation raw response: {}", responseBody);

      if (!StringUtils.hasText(responseBody)) {
        throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED);
      }

      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode results = root.path("results");
      if (!results.isArray() || results.isEmpty()) {
        throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED);
      }

      return results.get(0).path("flagged").asBoolean(false);
    } catch (HttpStatusCodeException e) {
      log.error("OpenAI moderation failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
      throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED, e);
    } catch (RestClientException e) {
      log.error("OpenAI moderation rest client error: {}", e.getMessage(), e);
      throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED, e);
    } catch (Exception e) {
      log.error("OpenAI moderation parse error: {}", e.getMessage(), e);
      throw new BaseException(ErrorCode.COMMENT_MODERATION_FAILED, e);
    }
  }
}
