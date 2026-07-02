package com.codeit.server.batch.job.articlecollect.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class NaverArticleItem {
    private String title;
    private String originallink;
    private String link;
    private String description;
    private String pubDate;
}

