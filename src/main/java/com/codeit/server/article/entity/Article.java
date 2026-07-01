package com.codeit.server.article.entity;

import com.codeit.server.global.common.BaseUpdatableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Entity
@Table(name = "articles")
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
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }
}