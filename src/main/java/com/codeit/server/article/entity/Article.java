package com.codeit.server.article.entity;

import com.codeit.server.global.common.BaseUpdatableEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Entity
@Table(name = "articles")
@EntityListeners(AuditingEntityListener.class)
public class Article extends BaseUpdatableEntity {

    @Column(nullable = false, length = 30)
    private String source;

    @Column(nullable = false, unique = true, length = 255)
    private String sourceUrl;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Instant publishDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @PositiveOrZero
    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @PositiveOrZero
    @Builder.Default
    @Column(nullable = false)
    private int commentCount = 0;


    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;


    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        this.commentCount--;
    }

    public void delete() {
        this.isDeleted = true;
    }

}

//CREATE TABLE articles (
//        id UUID PRIMARY KEY,
//        source VARCHAR(30) NOT NULL,
//        source_url VARCHAR(255) NOT NULL UNIQUE,
//        title VARCHAR(255) NOT NULL,
//        publish_date TIMESTAMP WITH TIME ZONE NOT NULL,
//        summary TEXT NOT NULL,
//        view_count INTEGER NOT NULL DEFAULT 0,
//        comment_count INTEGER NOT NULL DEFAULT 0,
//        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
//        updated_at TIMESTAMP WITH TIME ZONE,
//        is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
//
//        CONSTRAINT chk_articles_view_count
//            CHECK (view_count >= 0),
//
//        CONSTRAINT chk_articles_comment_count
//            CHECK (comment_count >= 0)
//);