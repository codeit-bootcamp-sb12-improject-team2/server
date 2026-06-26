package com.codeit.server.article.repository;

import com.codeit.server.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    boolean existsByTitle(String title);
}
