package com.codeit.server.comment.repository;

import com.codeit.server.comment.dto.CursorPageResponseCommentDto;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface CommentRepositoryCustom {

  CursorPageResponseCommentDto findAllByArticle(
      UUID articleId,
      UUID requestUserId,
      String orderBy,
      Sort.Direction direction,
      String cursor,
      Instant after,
      int limit
  );
}
