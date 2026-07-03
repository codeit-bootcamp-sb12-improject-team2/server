package com.codeit.server.article.repository;

import com.codeit.server.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    Optional<ArticleView> findByArticleIdAndUserId(UUID articleId, UUID userId);

}
