package com.codeit.server.rank.entity;

import com.codeit.server.article.entity.Article;
import com.codeit.server.global.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "article_rankings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ranking_date", "rank_type", "ranking"})
})
public class ArticleRanking extends BaseEntity {

    @Column(name = "ranking_date", nullable = false)
    private LocalDate rankingDate;

    @Column(name = "rank_type", nullable = false, length = 20)
    private String rankType;

    @Column(name = "ranking", nullable = false)
    private int ranking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "ranking_count", nullable = false)
    private long rankingCount;
}
