package com.codeit.server.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.article.entity.Article;
import com.codeit.server.article.repository.ArticleRepository;
import com.codeit.server.comment.dto.CommentDto;
import com.codeit.server.comment.dto.CommentLikeDto;
import com.codeit.server.comment.entity.Comment;
import com.codeit.server.comment.entity.CommentLike;
import com.codeit.server.comment.repository.CommentLikeRepository;
import com.codeit.server.comment.repository.CommentRepository;
import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.notification.event.NotificationEvent;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentModerationService commentModerationService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private CommentService commentService;

  @Nested
  @DisplayName("create 테스트")
  class Create {

    @Test
    @DisplayName("성공 - 댓글을 저장하고 기사 댓글 수를 증가시킨다")
    void success() {
      // Given
      UUID articleId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      String content = "좋은 기사입니다";

      User user = User.create("user@test.com", "monew", "pw");
      Article article = Article.builder()
          .source("NAVER")
          .sourceUrl("https://example.com/article")
          .title("title")
          .publishDate(Instant.now())
          .summary("summary")
          .commentCount(0)
          .build();
      Comment savedComment = Comment.builder()
          .id(UUID.randomUUID())
          .articleId(articleId)
          .userId(userId)
          .content(content)
          .likeCount(0L)
          .isDeleted(false)
          .build();

      when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
      when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
      when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
      doNothing().when(commentModerationService).validate(content);

      // When
      assertThat(article.getCommentCount()).isEqualTo(0);
      CommentDto result = commentService.create(articleId, userId, content);

      // Then
      assertThat(result.getContent()).isEqualTo(content);
      assertThat(result.getUserNickname()).isEqualTo("monew");
      assertThat(article.getCommentCount()).isEqualTo(1);
      verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("실패 - 모더레이션에서 차단되면 예외가 발생한다")
    void fail_blocked() {
      // Given
      UUID articleId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      String content = "bad word";

      doThrow(new BaseException(ErrorCode.COMMENT_CONTENT_BLOCKED))
          .when(commentModerationService).validate(content);

      // When & Then
      assertThatThrownBy(() -> commentService.create(articleId, userId, content))
          .isInstanceOf(BaseException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_CONTENT_BLOCKED);
    }
  }

  @Nested
  @DisplayName("update 테스트")
  class Update {

    @Test
    @DisplayName("성공 - 본인 댓글을 수정한다")
    void success() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      String content = "수정된 내용";

      User user = User.create("user@test.com", "writer", "pw");
      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(UUID.randomUUID())
          .userId(userId)
          .content("old")
          .likeCount(0L)
          .isDeleted(false)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
      when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
      when(commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(false);
      doNothing().when(commentModerationService).validate(content);

      // When
      CommentDto result = commentService.update(commentId, userId, content);

      // Then
      assertThat(result.getContent()).isEqualTo(content);
      assertThat(comment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("실패 - 다른 사람 댓글이면 권한 예외가 발생한다")
    void fail_accessDenied() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID requestUserId = UUID.randomUUID();

      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(UUID.randomUUID())
          .userId(ownerId)
          .content("old")
          .likeCount(0L)
          .isDeleted(false)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

      // When & Then
      assertThatThrownBy(() -> commentService.update(commentId, requestUserId, "new"))
          .isInstanceOf(BaseException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_ACCESS_DENIED);
    }
  }

  @Nested
  @DisplayName("delete 테스트")
  class Delete {

    @Test
    @DisplayName("성공 - 삭제 시 commentCount를 감소시키고 soft delete 한다")
    void success() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID articleId = UUID.randomUUID();
      Article article = Article.builder()
          .source("NAVER")
          .sourceUrl("https://example.com/article")
          .title("title")
          .publishDate(Instant.now())
          .summary("summary")
          .commentCount(1)
          .build();
      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(articleId)
          .userId(UUID.randomUUID())
          .content("content")
          .likeCount(0L)
          .isDeleted(false)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
      when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

      // When
      assertThat(article.getCommentCount()).isEqualTo(1);
      commentService.delete(commentId);

      // Then
      assertThat(comment.isDeleted()).isTrue();
      assertThat(article.getCommentCount()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("like/unlike 테스트")
  class LikeUnlike {

    @Test
    @DisplayName("성공 - 좋아요를 누르고 알림을 발행한다")
    void like_success() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID likerId = UUID.randomUUID();

      User liker = User.create("liker@test.com", "liker", "pw");
      User owner = User.create("owner@test.com", "owner", "pw");
      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(UUID.randomUUID())
          .userId(ownerId)
          .content("content")
          .likeCount(0L)
          .isDeleted(false)
          .build();
      CommentLike commentLike = CommentLike.builder()
          .id(UUID.randomUUID())
          .userId(likerId)
          .commentId(commentId)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
      when(userRepository.findByIdAndIsDeletedFalse(likerId)).thenReturn(Optional.of(liker));
      when(userRepository.findByIdAndIsDeletedFalse(ownerId)).thenReturn(Optional.of(owner));
      when(commentLikeRepository.existsByUserIdAndCommentId(likerId, commentId)).thenReturn(false);
      when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(commentLike);

      // When
      CommentLikeDto result = commentService.like(commentId, likerId);

      // Then
      assertThat(result.getCommentLikeCount()).isEqualTo(1);
      assertThat(comment.getLikeCount()).isEqualTo(1);
      verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("실패 - 이미 좋아요를 눌렀으면 예외가 발생한다")
    void like_fail_alreadyLiked() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(UUID.randomUUID())
          .userId(UUID.randomUUID())
          .content("content")
          .likeCount(0L)
          .isDeleted(false)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
      when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(User.create("u@test.com", "u", "pw")));
      when(commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> commentService.like(commentId, userId))
          .isInstanceOf(BaseException.class)
          .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_LIKED_COMMENT);
    }

    @Test
    @DisplayName("성공 - 좋아요 취소 시 likeCount를 감소시키고 like 기록을 삭제한다")
    void unlike_success() {
      // Given
      UUID commentId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      Comment comment = Comment.builder()
          .id(commentId)
          .articleId(UUID.randomUUID())
          .userId(UUID.randomUUID())
          .content("content")
          .likeCount(1L)
          .isDeleted(false)
          .build();
      CommentLike commentLike = CommentLike.builder()
          .id(UUID.randomUUID())
          .userId(userId)
          .commentId(commentId)
          .build();

      when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
      when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(User.create("u@test.com", "u", "pw")));
      when(commentLikeRepository.findByUserIdAndCommentId(userId, commentId)).thenReturn(Optional.of(commentLike));

      // When
      commentService.unlike(commentId, userId);

      // Then
      assertThat(comment.getLikeCount()).isEqualTo(0);
      verify(commentLikeRepository).delete(commentLike);
    }
  }
}
