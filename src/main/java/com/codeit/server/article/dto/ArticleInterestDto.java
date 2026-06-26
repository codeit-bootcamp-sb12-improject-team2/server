package com.codeit.server.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleInterestDto {
    private UUID id;
    private UUID articleId;
    private UUID interestId;
}
