package com.codeit.server.batch.job.articlecollect.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class NaverArticleResponse {
    private List<NaverArticleItem> items;
}