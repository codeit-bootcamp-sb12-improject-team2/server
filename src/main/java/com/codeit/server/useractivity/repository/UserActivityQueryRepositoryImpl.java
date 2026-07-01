package com.codeit.server.useractivity.repository;

import static com.codeit.server.user.entity.QUser.user;
import static com.codeit.server.interest.entity.QSubscription.subscription;
import static com.codeit.server.interest.entity.QInterest.interest;
import static com.codeit.server.interest.entity.QInterestKeyword.interestKeyword;
import static com.codeit.server.comment.entity.QComment.comment;
import static com.codeit.server.comment.entity.QCommentLike.commentLike;
import static com.codeit.server.article.entity.QArticleView.articleView;
import static com.codeit.server.article.entity.QArticle.article;

import com.querydsl.core.types.dsl.Expressions;

import com.codeit.server.useractivity.dto.ArticleActivityDto;
import com.codeit.server.useractivity.dto.CommentLikeUserActivityDto;
import com.codeit.server.useractivity.dto.CommentUserActivityDto;
import com.codeit.server.useractivity.dto.SubscriptionActivityDto;
import com.codeit.server.useractivity.dto.UserActivityDto.UserActivityDtoBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserActivityQueryRepositoryImpl implements UserActivityQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<UserActivityDtoBuilder> findUserAndSubscriptions(UUID userId) {

    com.querydsl.core.Tuple userTuple = queryFactory
        .select(
            user.id,
            user.email,
            user.nickname,
            user.createdAt
        )
        .from(user)
        .where(user.id.eq(userId).and(user.isDeleted.isFalse()))
        .fetchOne();

    if (userTuple == null) {
      return Optional.empty();
    }

    UserActivityDtoBuilder userInfoBuilder = com.codeit.server.useractivity.dto.UserActivityDto.builder()
        .id(userTuple.get(user.id) != null ? userTuple.get(user.id).toString() : null)
        .email(userTuple.get(user.email))
        .nickname(userTuple.get(user.nickname))
        .createdAt(userTuple.get(user.createdAt) != null ? userTuple.get(user.createdAt).toString() : null);

    List<SubscriptionActivityDto> subscriptionList = queryFactory
        .select(Projections.constructor(SubscriptionActivityDto.class,
            Expressions.stringTemplate("CAST({0} AS string)", subscription.id),
            Expressions.stringTemplate("CAST({0} AS string)", interest.id),
            interest.name,
            com.querydsl.core.types.dsl.Expressions.constant(new java.util.ArrayList<String>()),
            interest.subscriberCount.coalesce(0).longValue(), // Integer -> long 안전 변환 추가
            Expressions.stringTemplate("CAST({0} AS string)", subscription.createdAt)
        ))
        .from(subscription)
        .join(subscription.interest, interest)
        .where(subscription.user.id.eq(userId))
        .fetch();

    if (!subscriptionList.isEmpty()) {
      List<UUID> interestIds = queryFactory
          .select(subscription.interest.id)
          .from(subscription)
          .where(subscription.user.id.eq(userId))
          .fetch();

      Map<String, List<String>> keywordMap = queryFactory
          .select(Expressions.stringTemplate("CAST({0} AS string)", interestKeyword.interest.id),
              interestKeyword.keyword)
          .from(interestKeyword)
          .where(interestKeyword.interest.id.in(interestIds))
          .fetch()
          .stream()
          .collect(Collectors.groupingBy(
              tuple -> tuple.get(0, String.class),
              Collectors.mapping(tuple -> tuple.get(1, String.class), Collectors.toList())
          ));

      subscriptionList = subscriptionList.stream().map(sub ->
          SubscriptionActivityDto.builder()
              .id(sub.getId())
              .interestId(sub.getInterestId())
              .interestName(sub.getInterestName())
              .interestKeywords(keywordMap.getOrDefault(sub.getInterestId(), List.of()))
              .interestSubscriberCount(sub.getInterestSubscriberCount())
              .createdAt(sub.getCreatedAt())
              .build()
      ).toList();
    }
    userInfoBuilder.subscriptions(subscriptionList);
    return Optional.of(userInfoBuilder);
  }

  @Override
  public List<CommentUserActivityDto> findRecentCommentsByUserId(UUID userId, int limit) {
    return queryFactory
        .select(Projections.constructor(CommentUserActivityDto.class,
            Expressions.stringTemplate("CAST({0} AS string)", comment.id),
            Expressions.stringTemplate("CAST({0} AS string)", comment.articleId),
            article.title,
            Expressions.stringTemplate("CAST({0} AS string)", comment.userId),
            user.nickname,
            comment.content,
            comment.likeCount,
            Expressions.stringTemplate("CAST({0} AS string)", comment.createdAt)
        ))
        .from(comment)
        .join(article).on(comment.articleId.eq(article.id))
        .join(user).on(comment.userId.eq(user.id))
        .where(comment.userId.eq(userId).and(comment.isDeleted.isFalse()))
        .orderBy(comment.createdAt.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<CommentLikeUserActivityDto> findRecentCommentLikesByUserId(UUID userId, int limit) {
    return queryFactory
        .select(Projections.constructor(CommentLikeUserActivityDto.class,
            Expressions.stringTemplate("CAST({0} AS string)", commentLike.id),
            Expressions.stringTemplate("CAST({0} AS string)", commentLike.createdAt),
            Expressions.stringTemplate("CAST({0} AS string)", commentLike.commentId),
            Expressions.stringTemplate("CAST({0} AS string)", comment.articleId),
            article.title, // 원본 기사 제목
            Expressions.stringTemplate("CAST({0} AS string)", comment.userId), // 댓글 원본 작성자 ID
            user.nickname, // 댓글 원본 작성자 닉네임
            comment.content,
            comment.likeCount,
            Expressions.stringTemplate("CAST({0} AS string)", comment.createdAt) // 댓글 원본 생성일
        ))
        .from(commentLike)
        .join(comment).on(commentLike.commentId.eq(comment.id))
        .join(article).on(comment.articleId.eq(article.id))
        .join(user).on(comment.userId.eq(user.id))
        .where(commentLike.userId.eq(userId))
        .orderBy(commentLike.createdAt.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<ArticleActivityDto> findRecentArticleViewsByUserId(UUID userId, int limit) {
    return queryFactory
        .select(Projections.constructor(ArticleActivityDto.class,
            Expressions.stringTemplate("CAST({0} AS string)", articleView.id),
            article.source,
            article.sourceUrl,
            article.title,
            Expressions.stringTemplate("CAST({0} AS string)", article.publishDate),
            article.summary,
            article.commentCount.longValue(),
            article.viewCount.longValue(),
            articleView.userId.eq(userId) // 내가 본 뉴스 탭이므로 무조건 true로 판별 바인딩
        ))
        .from(articleView)
        .join(article).on(articleView.articleId.eq(article.id))
        .where(articleView.userId.eq(userId).and(article.isDeleted.isFalse()))
        .orderBy(articleView.createdAt.desc())
        .limit(limit)
        .fetch();
  }

}
