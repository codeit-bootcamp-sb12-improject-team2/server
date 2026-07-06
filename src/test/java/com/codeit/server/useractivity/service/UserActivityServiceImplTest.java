package com.codeit.server.useractivity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.useractivity.dto.ArticleActivityDto;
import com.codeit.server.useractivity.dto.CommentLikeUserActivityDto;
import com.codeit.server.useractivity.dto.CommentUserActivityDto;
import com.codeit.server.useractivity.dto.UserActivityDto;
import com.codeit.server.useractivity.repository.UserActivityQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceImplTest {

  @Mock
  private UserActivityQueryRepository userActivityQueryRepository;

  @InjectMocks
  private UserActivityServiceImpl userActivityService;

  @Test
  @DisplayName("실패 - userId가 유효한 UUID 형식이 아닐 때 INVALID_REQUEST 예외가 발생한다")
  void fail_invalidUuid() {
    // Given
    String invalidUserId = "invalid-uuid-string";

    // When & Then
    assertThatThrownBy(() -> userActivityService.getUserActivity(invalidUserId))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("실패 - 사용자가 존재하지 않을 때 USER_NOT_FOUND 예외가 발생한다")
  void fail_userNotFound() {
    // Given
    UUID userId = UUID.randomUUID();
    String userIdStr = userId.toString();

    when(userActivityQueryRepository.findUserAndSubscriptions(userId))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userActivityService.getUserActivity(userIdStr))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("성공 - 사용자의 활동 이력을 정상적으로 조회한다")
  void success() {
    // Given
    UUID userId = UUID.randomUUID();
    String userIdStr = userId.toString();

    UserActivityDto.UserActivityDtoBuilder builder = UserActivityDto.builder()
        .id(userIdStr)
        .email("test@example.com")
        .nickname("testUser")
        .createdAt("2026-07-06T00:00:00Z")
        .subscriptions(List.of());

    List<CommentUserActivityDto> comments = List.of(
        CommentUserActivityDto.builder().id(UUID.randomUUID().toString()).content("댓글").build()
    );
    List<CommentLikeUserActivityDto> commentLikes = List.of(
        CommentLikeUserActivityDto.builder().commentId(UUID.randomUUID().toString()).articleId(UUID.randomUUID().toString()).build()
    );
    List<ArticleActivityDto> articleViews = List.of(
        ArticleActivityDto.builder().articleId(UUID.randomUUID().toString()).title("기사제목").build()
    );

    when(userActivityQueryRepository.findUserAndSubscriptions(userId))
        .thenReturn(Optional.of(builder));
    when(userActivityQueryRepository.findRecentCommentsByUserId(userId, 10))
        .thenReturn(comments);
    when(userActivityQueryRepository.findRecentCommentLikesByUserId(userId, 10))
        .thenReturn(commentLikes);
    when(userActivityQueryRepository.findRecentArticleViewsByUserId(userId, 10))
        .thenReturn(articleViews);

    // When
    UserActivityDto result = userActivityService.getUserActivity(userIdStr);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userIdStr);
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    assertThat(result.getComments()).hasSize(1);
    assertThat(result.getCommentLikes()).hasSize(1);
    assertThat(result.getArticleViews()).hasSize(1);

    verify(userActivityQueryRepository).findUserAndSubscriptions(userId);
    verify(userActivityQueryRepository).findRecentCommentsByUserId(userId, 10);
    verify(userActivityQueryRepository).findRecentCommentLikesByUserId(userId, 10);
    verify(userActivityQueryRepository).findRecentArticleViewsByUserId(userId, 10);
  }
}
