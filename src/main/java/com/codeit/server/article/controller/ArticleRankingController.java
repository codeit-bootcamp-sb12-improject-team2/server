package com.codeit.server.article.controller;

import com.codeit.server.article.dto.ArticleRankingResponse;
import com.codeit.server.article.service.ArticleRankingService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/rankings")
public class ArticleRankingController {
  private static final int RANKING_LIMIT = 3;

  private final ArticleRankingService articleRankingService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public ArticleRankingResponse getDailyRanking(
      @RequestParam(value = "type", defaultValue = "VIEW")
      String rankType
  ) {
    return articleRankingService.getDailyRanking(
        LocalDate.now(),
        rankType,
        RANKING_LIMIT
    );
  }
}
