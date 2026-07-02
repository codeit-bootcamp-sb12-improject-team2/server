package com.codeit.server.article.service;

import com.codeit.server.article.dto.ArticleRankingResponse;
import java.time.LocalDate;

public interface ArticleRankingService {
  ArticleRankingResponse getTodayRanking(
      LocalDate date,
      String rankType,
      int limit
  );
}
