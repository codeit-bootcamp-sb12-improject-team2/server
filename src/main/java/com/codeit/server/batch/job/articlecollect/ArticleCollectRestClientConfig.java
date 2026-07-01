package com.codeit.server.batch.job.articlecollect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ArticleCollectRestClientConfig {

    @Bean
    public RestClient articleCollectRestClient() {
        return RestClient.builder().build();
    }
}