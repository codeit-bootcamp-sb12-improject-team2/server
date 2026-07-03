package com.codeit.server.useractivity.repository;

import com.codeit.server.useractivity.dto.ArticleActivityDto;
import com.codeit.server.useractivity.dto.CommentLikeUserActivityDto;
import com.codeit.server.useractivity.dto.CommentUserActivityDto;
import com.codeit.server.useractivity.dto.UserActivityDto.UserActivityDtoBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserActivityQueryRepository {

  Optional<UserActivityDtoBuilder> findUserAndSubscriptions(UUID userId);
  List<CommentUserActivityDto> findRecentCommentsByUserId(UUID userId, int limit);
  List<CommentLikeUserActivityDto> findRecentCommentLikesByUserId(UUID userId, int limit);
  List<ArticleActivityDto> findRecentArticleViewsByUserId(UUID userId, int limit);

}
