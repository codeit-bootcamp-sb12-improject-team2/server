package com.codeit.server.article.service;

import com.codeit.server.article.dto.ArticleRankingDto;
import com.codeit.server.article.dto.ArticleRankingResponse;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleRankingServiceImpl implements ArticleRankingService {
  private final ArticleRepository articleRepository;

  @Override
  public ArticleRankingResponse getTodayRanking(LocalDate date, String rankType) {
    String normalizedType = rankType.toUpperCase(Locale.ROOT);
    ZoneId zone = ZoneId.of("Asia/Seoul");
    Instant start = date.atStartOfDay(zone).toInstant();
    Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

    List<ArticleRankingDto> articles = switch (normalizedType) {
      case "VIEW" -> articleRepository.findTopArticlesByViewCount(start, end);
      case "COMMENT" -> articleRepository.findTopArticlesByCommentCount(start, end);
      default -> throw new BaseException(ErrorCode.INVALID_RANKING_TYPE);
    };
    return ArticleRankingResponse.of(date, normalizedType, articles);
  }
}
