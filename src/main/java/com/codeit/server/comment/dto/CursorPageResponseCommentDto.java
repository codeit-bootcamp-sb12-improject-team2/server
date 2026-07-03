package com.codeit.server.comment.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorPageResponseCommentDto {

  private List<CommentDto> content;
  private String nextCursor;
  private Instant nextAfter;
  private int size;
  private long totalElements;
  private boolean hasNext;

}
