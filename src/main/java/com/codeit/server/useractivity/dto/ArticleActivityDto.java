package com.codeit.server.useractivity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleActivityDto {

  private final String id;
  private final String source;
  private final String sourceUrl;
  private final String title;
  private final String publishDate;
  private final String summary;
  private final long commentCount;
  private final long viewCount;
  private final boolean viewedByMe;

}
