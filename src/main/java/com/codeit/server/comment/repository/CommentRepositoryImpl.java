package com.codeit.server.comment.repository;

import static com.codeit.server.comment.entity.QComment.comment;
import static com.codeit.server.comment.entity.QCommentLike.commentLike;
import static com.codeit.server.user.entity.QUser.user;

import com.codeit.server.comment.dto.CommentDto;
import com.codeit.server.comment.dto.CursorPageResponseCommentDto;
import com.codeit.server.comment.entity.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
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
  ) {
    int pageSize = Math.max(limit, 1);
    boolean asc = direction == Sort.Direction.ASC;
    String sortKey = "likeCount".equals(orderBy) ? "likeCount" : "createdAt";
    UUID cursorId = (cursor == null || cursor.isBlank()) ? null : UUID.fromString(cursor);

    BooleanBuilder where = new BooleanBuilder();
    where.and(comment.articleId.eq(articleId));
    where.and(comment.isDeleted.isFalse());

    if (cursorId != null && after != null) {
      applyCursorCondition(where, sortKey, asc, cursorId, after);
    }

    List<CommentDto> content = queryFactory
        .select(Projections.constructor(
            CommentDto.class,
            comment.id,
            comment.articleId,
            comment.userId,
            user.nickname,
            comment.content,
            comment.likeCount,
            new CaseBuilder()
                .when(commentLike.id.isNotNull())
                .then(true)
                .otherwise(false),
            comment.createdAt
        ))
        .from(comment)
        .join(user).on(user.id.eq(comment.userId))
        .leftJoin(commentLike).on(
            commentLike.commentId.eq(comment.id),
            commentLike.userId.eq(requestUserId)
        )
        .where(where)
        .orderBy(getOrderSpecifiers(sortKey, asc))
        .limit(pageSize + 1L)
        .fetch();

    boolean hasNext = content.size() > pageSize;
    List<CommentDto> resultPage = hasNext ? content.subList(0, pageSize) : content;
    CommentDto lastItem = resultPage.isEmpty() ? null : resultPage.get(resultPage.size() - 1);

    Long totalElements = queryFactory
        .select(comment.count())
        .from(comment)
        .where(
            comment.articleId.eq(articleId),
            comment.isDeleted.isFalse()
        )
        .fetchOne();

    return CursorPageResponseCommentDto.builder()
        .content(resultPage)
        .nextCursor(lastItem != null ? lastItem.getId().toString() : null)
        .nextAfter(lastItem != null ? lastItem.getCreatedAt() : null)
        .size(resultPage.size())
        .totalElements(totalElements != null ? totalElements : 0L)
        .hasNext(hasNext)
        .build();
  }


  private void applyCursorCondition(
      BooleanBuilder where,
      String sortKey,
      boolean asc,
      UUID cursorId,
      Instant after
  ) {
    if ("likeCount".equals(sortKey)) {
      QComment cursorComment = new QComment("cursorComment");
      Long cursorLikeCount = queryFactory
          .select(cursorComment.likeCount)
          .from(cursorComment)
          .where(cursorComment.id.eq(cursorId))
          .fetchOne();

      if (cursorLikeCount == null) {
        return;
      }

      if (asc) {
        where.and(
            comment.likeCount.gt(cursorLikeCount)
                .or(comment.likeCount.eq(cursorLikeCount).and(comment.createdAt.gt(after)))
                .or(comment.likeCount.eq(cursorLikeCount)
                    .and(comment.createdAt.eq(after))
                    .and(comment.id.gt(cursorId)))
        );
      } else {
        where.and(
            comment.likeCount.lt(cursorLikeCount)
                .or(comment.likeCount.eq(cursorLikeCount).and(comment.createdAt.lt(after)))
                .or(comment.likeCount.eq(cursorLikeCount)
                    .and(comment.createdAt.eq(after))
                    .and(comment.id.lt(cursorId)))
        );
      }
      return;
    }

    if (asc) {
      where.and(
          comment.createdAt.gt(after)
              .or(comment.createdAt.eq(after).and(comment.id.gt(cursorId)))
      );
    } else {
      where.and(
          comment.createdAt.lt(after)
              .or(comment.createdAt.eq(after).and(comment.id.lt(cursorId)))
      );
    }
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(String sortKey, boolean asc) {
    if ("likeCount".equals(sortKey)) {
      return asc
          ? new OrderSpecifier<?>[]{
          comment.likeCount.asc(),
          comment.createdAt.asc(),
          comment.id.asc()
      }
          : new OrderSpecifier<?>[]{
              comment.likeCount.desc(),
              comment.createdAt.desc(),
              comment.id.desc()
          };
    }

    return asc
        ? new OrderSpecifier<?>[]{
        comment.createdAt.asc(),
        comment.id.asc()
    }
        : new OrderSpecifier<?>[]{
            comment.createdAt.desc(),
            comment.id.desc()
        };
  }
}

