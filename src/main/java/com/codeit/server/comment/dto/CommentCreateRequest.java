package com.codeit.server.comment.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

  @NotNull
  private UUID articleId;

  @NotNull
  private UUID userId;

  @NotBlank
  @Size(max = 255)
  private String content;

}
