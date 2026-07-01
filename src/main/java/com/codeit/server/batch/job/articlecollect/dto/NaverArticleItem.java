package com.codeit.server.batch.job.articlecollect.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverArticleItem {
    private String title;
    private String originallink;
    private String link;
    private String description;
    private String pubDate;
}

