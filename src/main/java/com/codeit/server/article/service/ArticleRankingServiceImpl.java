package com.codeit.server.article.service;

import com.codeit.server.article.dto.ArticleRankingDto;
import com.codeit.server.article.dto.ArticleRankingResponse;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleRankingServiceImpl implements ArticleRankingService {
  private final ArticleRepository articleRepository;

  @Override
  public ArticleRankingResponse getTodayRanking(LocalDate date, String rankType, int limit) {
    String normalizedType = rankType.toUpperCase();

    List<ArticleRankingDto> articles = switch (normalizedType) {
      case "VIEW" -> articleRepository.findTodayTopArticlesByViewCount(date, limit);
      case "COMMENT" -> articleRepository.findTodayTopArticlesByCommentCount(date, limit);
      default -> throw new BaseException(ErrorCode.INVALID_RANKING_TYPE);
    };
    return ArticleRankingResponse.of(date, normalizedType, articles);
  }
}
