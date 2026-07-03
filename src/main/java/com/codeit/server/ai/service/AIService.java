package com.codeit.server.ai.service;

import com.codeit.server.ai.dto.NewsSummaryResponseDto;
import java.util.UUID;

public interface AIService {

  NewsSummaryResponseDto summarizeNews(UUID articleId);

}

