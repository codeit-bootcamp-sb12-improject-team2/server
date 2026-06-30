package com.codeit.server.article.repository;

import com.codeit.server.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId); // 한 명이 여러번 조회수 상승 방지

}
