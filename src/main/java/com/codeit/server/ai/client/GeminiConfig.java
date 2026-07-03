package com.codeit.server.ai.client;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GeminiConfig {

  @Value("${gemini.api-key}")
  String GEMINI_API_KEY;

  @Bean
  public Client getClient() {
    return Client.builder()
        .apiKey(GEMINI_API_KEY)
        .build();
  }

}
