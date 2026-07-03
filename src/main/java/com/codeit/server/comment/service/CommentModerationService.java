package com.codeit.server.comment.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentModerationService {

  private final CommentModerationClient moderationClient;
  private Set<String> blockedWords;

  @PostConstruct
  void loadBlockedWords() {
    ClassPathResource resource = new ClassPathResource("comment-blocked-words.txt");
    try (InputStream inputStream = resource.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      blockedWords = reader.lines()
          .map(String::trim)
          .filter(StringUtils::hasText)
          .map(word -> word.toLowerCase(Locale.ROOT))
          .collect(java.util.stream.Collectors.toSet());
    } catch (IOException e) {
      log.warn("Failed to load comment-blocked-words.txt, local blocked words will be disabled.", e);
      blockedWords = Set.of();
    }
  }

  public void validate(String content) {
    if (content == null || content.isBlank()) {
      throw new BaseException(ErrorCode.COMMENT_CONTENT_BLOCKED);
    }

    if (isLocallyBlocked(content)) {
      log.info("Comment blocked by local blocked-words rule");
      throw new BaseException(ErrorCode.COMMENT_CONTENT_BLOCKED);
    }

    if (moderationClient.isFlagged(content)) {
      log.info("Comment blocked by moderation api");
      throw new BaseException(ErrorCode.COMMENT_CONTENT_BLOCKED);
    }
  }

  private boolean isLocallyBlocked(String content) {
    if (blockedWords == null || blockedWords.isEmpty()) {
      return false;
    }

    String normalizedContent = content.toLowerCase(Locale.ROOT);
    return blockedWords.stream()
        .anyMatch(normalizedContent::contains);
  }
}
