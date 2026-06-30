package com.codeit.server.article.controller;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.ArticleViewDto;
import com.codeit.server.article.dto.CursorPageResponseArticle;
import com.codeit.server.article.service.ArticleService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private static final String REQUEST_USER_ID_HEADER = "Monew-Request-User-ID";

    private final ArticleService articleService;

    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> createArticleView(
            @PathVariable UUID articleId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        return ResponseEntity.ok(
                articleService.createArticleView(articleId, requestUserId));
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseArticle> findArticles(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId,
            @ModelAttribute ArticleSearchRequest request
    ) {
        return ResponseEntity.ok(
                articleService.findArticles(
                        cursor,
                        after,
                        limit,
                        requestUserId,
                        request
                )
        );
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> findArticle(
            @PathVariable UUID articleId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requestUserId
    ) {
        return ResponseEntity.ok(
                articleService.findArticle(articleId, requestUserId));
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable UUID articleId
    ) {
        articleService.deleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sources")
    public ResponseEntity<List<String>> findSource() {
        return ResponseEntity.ok(articleService.findSource());
    }

    @GetMapping("/restore")
    public ResponseEntity<Void> restoreArticles(
            @RequestParam String from,
            @RequestParam String to
    ) {
        // TODO: Article restore service 구현 후 연결
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{articleId}/hard")
    public ResponseEntity<Void> hardDeleteArticle(
            @PathVariable UUID articleId
    ) {
        articleService.hardDeleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }
}