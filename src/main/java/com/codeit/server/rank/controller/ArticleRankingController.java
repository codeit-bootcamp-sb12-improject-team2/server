package com.codeit.server.rank.controller;

import com.codeit.server.rank.service.ArticleRankingService;
import java.time.LocalDate;
import java.time.ZoneId;

import com.codeit.server.rank.dto.ArticleRankingResponse;
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
  private final ArticleRankingService articleRankingService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public ArticleRankingResponse getDailyRanking(
      @RequestParam(value = "type", defaultValue = "VIEW")
      String rankType
  ) {
    return articleRankingService.getTodayRanking(
        LocalDate.now(ZoneId.of("Asia/Seoul")),
        rankType
    );
  }
}
