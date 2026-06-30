package com.codeit.server.comment.repository;

import com.codeit.server.comment.dto.CursorPageResponseCommentDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponseCommentDto findAllByArticle(
      UUID articleId,
      UUID requestUserId,
      String orderBy,
      Sort.Direction direction,
      String cursor,
      Instant after,
      int limit
  ){
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
