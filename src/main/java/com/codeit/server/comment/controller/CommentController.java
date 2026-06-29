package com.codeit.server.comment.controller;

import com.codeit.server.comment.dto.CommentCreateRequest;
import com.codeit.server.comment.dto.CommentDto;
import com.codeit.server.comment.dto.CommentLikeDto;
import com.codeit.server.comment.dto.CommentUpdateRequest;
import com.codeit.server.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> create(@Valid @RequestBody CommentCreateRequest request) {
    CommentDto comment = commentService.create(
        request.getArticleId(),
        request.getUserId(),
        request.getContent()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(comment);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> update(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID userId,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    CommentDto comment = commentService.update(commentId, userId, request.getContent());
    return ResponseEntity.ok(comment);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID commentId
  ) {
    commentService.delete(commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID commentId
  ) {
    commentService.hardDelete(commentId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeDto> like(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    return ResponseEntity.ok(commentService.like(commentId, userId));
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public ResponseEntity<Void> unlike(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    commentService.unlike(commentId, userId);
    return ResponseEntity.ok().build();
  }
}
