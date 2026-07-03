package com.codeit.server.rank.service;

import com.codeit.server.rank.dto.ArticleRankingDto;
import com.codeit.server.rank.dto.ArticleRankingResponse;
import com.codeit.server.rank.entity.ArticleRanking;
import com.codeit.server.rank.repository.ArticleRankingRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleRankingServiceImpl implements ArticleRankingService {
  private final ArticleRankingRepository articleRankingRepository;

  @Override
  public ArticleRankingResponse getTodayRanking(LocalDate date, String rankType) {
    String normalizedType = rankType.toUpperCase(Locale.ROOT);
    if (!normalizedType.equals("VIEW") && !normalizedType.equals("COMMENT")) {
      throw new BaseException(ErrorCode.INVALID_RANKING_TYPE);
    }

    LocalDate queryDate = date;
    List<ArticleRanking> rankings = articleRankingRepository.findByRankingDateAndRankTypeOrderByRankingAsc(queryDate, normalizedType);

    if (rankings.isEmpty()) {
      queryDate = date.minusDays(1);
      rankings = articleRankingRepository.findByRankingDateAndRankTypeOrderByRankingAsc(queryDate, normalizedType);
    }

    List<ArticleRankingDto> articles = rankings.stream()
        .map(r -> ArticleRankingDto.of(r.getRanking(), r.getArticle(), r.getRankingCount()))
        .toList();

    return ArticleRankingResponse.of(queryDate, normalizedType, articles);
  }
}
