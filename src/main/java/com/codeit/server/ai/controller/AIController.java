package com.codeit.server.ai.controller;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import com.codeit.server.ai.service.AIService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

  private final AIService aiService;

  @GetMapping("/summary")
  public NewsSummaryResponseDto summarize(
      @RequestParam UUID articleId
  ) {
    return aiService.summarizeNews(articleId);
  }

}


