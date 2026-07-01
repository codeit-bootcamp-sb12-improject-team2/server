package com.codeit.server.article.service;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.ArticleViewDto;
import com.codeit.server.article.dto.CursorPageResponseArticle;
import com.codeit.server.article.entity.Article;
import com.codeit.server.article.entity.ArticleView;
import com.codeit.server.article.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService{
    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
//    private final ArticleInterestRepository articleInterestRepository;

    @Transactional
    @Override
    public ArticleViewDto createArticleView(UUID articleId, UUID requestUserId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요

        ArticleView articleView = articleViewRepository
                .findByArticleIdAndUserId(articleId, requestUserId)
                .orElseGet(() -> {
                    ArticleView newView = ArticleView.builder()
                            .articleId(articleId)
                            .userId(requestUserId)
                            .build();

                    article.increaseViewCount(); // dirty checking 트랜잭션 때문에!!!

                    return articleViewRepository.save(newView);
                });

        return ArticleViewDto.from(article, articleView);

    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseArticle findArticles(String cursor, String after, int size, UUID requestUserId, ArticleSearchRequest request) {
        return articleRepository.searchArticles(cursor, after, size, requestUserId, request);
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleDto findArticle(UUID articleId, UUID requestUserId) {
        return articleRepository.findArticle(articleId, requestUserId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요;
    }

    @Transactional
    @Override
    public void deleteArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요
        article.delete();

    }

    @Transactional
    @Override
    public void hardDeleteArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(); //TODO : 예외 추후 글로벌 따라 변경 필요
        articleRepository.delete(article);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> findSource() {
        return articleRepository.findSource();
    }
}
