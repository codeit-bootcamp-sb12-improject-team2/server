package com.codeit.server.batch.job.articlecollect.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverArticleResponse {
    private List<NaverArticleItem> items;
}