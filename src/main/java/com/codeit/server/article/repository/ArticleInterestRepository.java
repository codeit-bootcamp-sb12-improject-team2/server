package com.codeit.server.article.repository;

import com.codeit.server.article.entity.ArticleInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleInterestRepository extends JpaRepository<ArticleInterest, UUID> {
    boolean existsByArticleIdAndInterestId(UUID articleId, UUID interestId);
}
