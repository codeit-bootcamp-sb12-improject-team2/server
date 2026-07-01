package com.codeit.server.useractivity.repository;

import com.codeit.server.useractivity.dto.ArticleActivityDto;
import com.codeit.server.useractivity.dto.CommentLikeUserActivityDto;
import com.codeit.server.useractivity.dto.CommentUserActivityDto;
import com.codeit.server.useractivity.dto.UserActivityDto.UserActivityDtoBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserActivityQueryRepositoryImpl implements UserActivityQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<UserActivityDtoBuilder> findUserAndSubscriptions(UUID userId) {
    return Optional.empty();
  }

  @Override
  public List<CommentUserActivityDto> findRecentCommentsByUserId(UUID userId, int limit) {
    return List.of();
  }

  @Override
  public List<CommentLikeUserActivityDto> findRecentCommentLikesByUserId(UUID userId, int limit) {
    return List.of();
  }

  @Override
  public List<ArticleActivityDto> findRecentArticleViewsByUserId(UUID userId, int limit) {
    return List.of();
  }

}
