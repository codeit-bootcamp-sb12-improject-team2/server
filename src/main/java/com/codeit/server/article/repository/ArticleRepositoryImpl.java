package com.codeit.server.article.repository;

import com.codeit.server.article.dto.*;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.QArticle;
import com.codeit.server.article.entity.QArticleInterest;
import com.codeit.server.article.entity.QArticleView;
import com.codeit.server.batch.job.articlebackup.dto.ArticleBackupDto;
import com.codeit.server.comment.entity.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  private static final QArticle a = QArticle.article;
  private static final QArticleView av = QArticleView.articleView;
  private static final QComment c = QComment.comment;
  private static final QArticleInterest ai = QArticleInterest.articleInterest;
  private static final NumberPath<Long> rankingCount = Expressions.numberPath(Long.class, "rankingCount");

  private final Expression<Boolean> viewedByMe =
      new CaseBuilder()
          .when(av.id.isNotNull())
          .then(true)
          .otherwise(false);

  @Override
  public Optional<ArticleDto> findArticle(UUID articleId, UUID requestUserId) {
    ArticleDto result = queryFactory
        .select(Projections.constructor(
            ArticleDto.class,
            a.id,
            a.source,
            a.sourceUrl,
            a.title,
            a.publishDate,
            a.summary,
            a.commentCount,
            a.viewCount,
            viewedByMe
        ))
        .from(a)
        .leftJoin(av)
        .on(
            av.articleId.eq(a.id),
            av.userId.eq(requestUserId)
        )
        .where(
            a.id.eq(articleId),
            a.isDeleted.isFalse()
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public CursorPageResponseArticle searchArticles(
      String cursor,
      String after,
      int size,
      UUID requestUserId,
      ArticleSearchRequest request
  ) {
    boolean isDesc = "desc".equalsIgnoreCase(request.getDirection());
    String orderBy = resolveOrderBy(request.getOrderBy());

    BooleanBuilder where = buildSearchCondition(request);
    applyCursorCondition(where, cursor, after, orderBy, isDesc);

    List<ArticleQueryDto> rows = queryFactory
        .select(Projections.constructor(
            ArticleQueryDto.class,
            a.id,
            a.source,
            a.sourceUrl,
            a.title,
            a.publishDate,
            a.summary,
            a.commentCount,
            a.viewCount,
            viewedByMe,
            a.createdAt
        ))
        .from(a)
        .leftJoin(av)
        .on(
            av.articleId.eq(a.id),
            av.userId.eq(requestUserId)
        )
        .where(where)
        .orderBy(
            getOrderSpecifier(orderBy, isDesc),
            isDesc ? a.createdAt.desc() : a.createdAt.asc()
        )
        .limit(size + 1L)
        .fetch();

    boolean hasNext = rows.size() > size;
    List<ArticleQueryDto> slicedRows = hasNext ? rows.subList(0, size) : rows;

    List<ArticleDto> content = slicedRows.stream()
        .map(ArticleQueryDto::toArticleDto)
        .toList();

    String nextCursor = null;
    String nextAfter = null;

    if (hasNext && !slicedRows.isEmpty()) {
      ArticleQueryDto last = slicedRows.get(slicedRows.size() - 1);

      nextCursor = createNextCursor(last, orderBy);
      nextAfter = last.getCreatedAt().toString();
    }

    return new CursorPageResponseArticle(
        content,
        nextCursor,
        nextAfter,
        size,
        countSearch(request),
        hasNext
    );
  }

  @Override
  public List<String> findSource() {
    return queryFactory
        .select(a.source)
        .distinct()
        .from(a)
        .where(a.isDeleted.isFalse())
        .orderBy(a.source.asc())
        .fetch();
  }

  @Override
  public List<ArticleBackupDto> findBackup(
      Instant start,
      Instant end
  ) {
    return queryFactory
        .select(Projections.constructor(
            ArticleBackupDto.class,
            a.id,
            a.source,
            a.sourceUrl,
            a.title,
            a.publishDate,
            a.summary,
            a.viewCount,
            a.commentCount,
            a.createdAt,
            a.updatedAt,
            a.isDeleted
        ))
        .from(a)
        .where(
            a.createdAt.goe(start),
            a.createdAt.lt(end)
        )
        .orderBy(a.createdAt.asc())
        .fetch();
  }

  @Override
  public void restoreAuditFields(UUID articleId, Instant createdAt, Instant updatedAt) {
    queryFactory
        .update(a)
        .set(a.createdAt, createdAt)
        .set(a.updatedAt, updatedAt)
        .where(a.id.eq(articleId))
        .execute();
  }

  @Override
  public List<ArticleRankingDto> findTopArticlesByViewCount(Instant start, Instant end) {
    return findTopArticles(start, end, true);
  }

  @Override
  public List<ArticleRankingDto> findTopArticlesByCommentCount(Instant start, Instant end) {
    return findTopArticles(start, end, false);
  }

  private BooleanBuilder buildSearchCondition(ArticleSearchRequest request) {
    BooleanBuilder where = new BooleanBuilder();

    where.and(a.isDeleted.isFalse());

    String keyword = request.getKeyword();
    if (keyword != null && !keyword.isBlank()) {
      where.and(
          a.title.containsIgnoreCase(keyword)
              .or(a.summary.containsIgnoreCase(keyword))
      );
    }

    if (request.getInterestId() != null) {
      where.and(
          JPAExpressions
              .selectOne()
              .from(ai)
              .where(
                  ai.articleId.eq(a.id),
                  ai.interestId.eq(request.getInterestId())
              )
              .exists()
      );
    }

    if (request.getSourceIn() != null && !request.getSourceIn().isEmpty()) {
      where.and(a.source.in(request.getSourceIn()));
    }

    if (request.getPublishDateFrom() != null) {
      where.and(a.publishDate.goe(request.getPublishDateFrom()));
    }

    if (request.getPublishDateTo() != null) {
      where.and(a.publishDate.loe(request.getPublishDateTo()));
    }

    return where;
  }

  private void applyCursorCondition(
      BooleanBuilder where,
      String cursor,
      String after,
      String orderBy,
      boolean isDesc
  ) {
    if (cursor == null || cursor.isBlank() || after == null || after.isBlank()) {
      return;
    }

    Instant cursorAfter = Instant.parse(after);

    switch (orderBy) {
      case "publishDate" -> {
        Instant cursorPublishDate = Instant.parse(cursor);

        where.and(isDesc
            ? a.publishDate.lt(cursorPublishDate)
            .or(a.publishDate.eq(cursorPublishDate)
                .and(a.createdAt.lt(cursorAfter)))
            : a.publishDate.gt(cursorPublishDate)
                .or(a.publishDate.eq(cursorPublishDate)
                    .and(a.createdAt.gt(cursorAfter)))
        );
      }

      case "viewCount" -> {
        int cursorViewCount = Integer.parseInt(cursor);

        where.and(isDesc
            ? a.viewCount.lt(cursorViewCount)
            .or(a.viewCount.eq(cursorViewCount)
                .and(a.createdAt.lt(cursorAfter)))
            : a.viewCount.gt(cursorViewCount)
                .or(a.viewCount.eq(cursorViewCount)
                    .and(a.createdAt.gt(cursorAfter)))
        );
      }

      case "commentCount" -> {
        int cursorCommentCount = Integer.parseInt(cursor);

        where.and(isDesc
            ? a.commentCount.lt(cursorCommentCount)
            .or(a.commentCount.eq(cursorCommentCount)
                .and(a.createdAt.lt(cursorAfter)))
            : a.commentCount.gt(cursorCommentCount)
                .or(a.commentCount.eq(cursorCommentCount)
                    .and(a.createdAt.gt(cursorAfter)))
        );
      }

      default -> {
        UUID cursorId = UUID.fromString(cursor);

        where.and(isDesc
            ? a.id.lt(cursorId)
            : a.id.gt(cursorId)
        );
      }
    }
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, boolean isDesc) {
    return switch (orderBy) {
      case "viewCount" -> isDesc ? a.viewCount.desc() : a.viewCount.asc();
      case "commentCount" -> isDesc ? a.commentCount.desc() : a.commentCount.asc();
      case "publishDate" -> isDesc ? a.publishDate.desc() : a.publishDate.asc();
      default -> isDesc ? a.publishDate.desc() : a.publishDate.asc();
    };
  }

  private String createNextCursor(ArticleQueryDto last, String orderBy) {
    return switch (orderBy) {
      case "publishDate" -> last.getPublishDate().toString();
      case "viewCount" -> String.valueOf(last.getViewCount());
      case "commentCount" -> String.valueOf(last.getCommentCount());
      default -> last.getPublishDate().toString();
    };
  }

  private String resolveOrderBy(String orderBy) {
    return orderBy == null || orderBy.isBlank()
        ? "publishDate"
        : orderBy;
  }

  private List<ArticleRankingDto> findTopArticles(Instant start, Instant end, boolean viewRank) {
    List<Tuple> rankedRows = viewRank
        ? queryTopArticlesByViewCount(start, end)
        : queryTopArticlesByCommentCount(start, end);

    if (rankedRows.isEmpty()) {
      return List.of();
    }

    List<UUID> articleIds = rankedRows.stream()
        .map(row -> row.get(a.id))
        .toList();

    Map<UUID, Article> articlesById = queryFactory
        .selectFrom(a)
        .where(
            a.isDeleted.isFalse(),
            a.id.in(articleIds)
        )
        .fetch()
        .stream()
        .collect(Collectors.toMap(Article::getId, article -> article));

    List<ArticleRankingDto> result = new java.util.ArrayList<>();
    int rank = 1;
    for (Tuple row : rankedRows) {
      UUID articleId = row.get(a.id);
      Long rankCount = row.get(rankingCount);
      Article article = articlesById.get(articleId);
      if (article == null) {
        continue;
      }

      result.add(ArticleRankingDto.of(
          rank++,
          article,
          rankCount
      ));
    }
    return result;
  }

  private List<Tuple> queryTopArticlesByViewCount(Instant start, Instant end) {
    NumberExpression<Long> viewCountExpr = av.id.count();

    return queryFactory
        .select(a.id, viewCountExpr.as(rankingCount))
        .from(a)
        .join(av).on(av.articleId.eq(a.id))
        .where(
            a.isDeleted.isFalse(),
            av.createdAt.goe(start),
            av.createdAt.lt(end)
        )
        .groupBy(a.id, a.createdAt)
        .orderBy(
            viewCountExpr.desc(),
            a.createdAt.desc(),
            a.id.desc()
        )
        .limit(3)
        .fetch();
  }

  private List<Tuple> queryTopArticlesByCommentCount(Instant start, Instant end) {
    NumberExpression<Long> commentCountExpr = c.id.count();

    return queryFactory
        .select(a.id, commentCountExpr.as(rankingCount))
        .from(a)
        .join(c).on(c.articleId.eq(a.id))
        .where(
            a.isDeleted.isFalse(),
            c.isDeleted.isFalse(),
            c.createdAt.goe(start),
            c.createdAt.lt(end)
        )
        .groupBy(a.id, a.createdAt)
        .orderBy(
            commentCountExpr.desc(),
            a.createdAt.desc(),
            a.id.desc()
        )
        .limit(3)
        .fetch();
  }

  private long countSearch(ArticleSearchRequest request) {
    Long count = queryFactory
        .select(a.count())
        .from(a)
        .where(buildSearchCondition(request))
        .fetchOne();

    return count == null ? 0L : count;
  }
}