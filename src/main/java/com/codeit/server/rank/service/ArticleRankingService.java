package com.codeit.server.rank.service;

import com.codeit.server.rank.dto.ArticleRankingResponse;
import java.time.LocalDate;

public interface ArticleRankingService {
  ArticleRankingResponse getTodayRanking(
      LocalDate date,
      String rankType
  );
}
