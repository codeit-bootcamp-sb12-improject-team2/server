package com.codeit.server.article.repository;

import com.codeit.server.article.dto.ArticleDto;
import com.codeit.server.article.dto.ArticleSearchRequest;
import com.codeit.server.article.dto.CursorPageResponseArticle;
import com.codeit.server.article.entity.QArticle;
import com.codeit.server.article.entity.QArticleInterest;
import com.codeit.server.article.entity.QArticleView;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    private static final QArticle a = QArticle.article;
    private static final QArticleView av = QArticleView.articleView;
    private static final QArticleInterest ai = QArticleInterest.articleInterest;

    @Override
    public Optional<ArticleDto> findArticle(UUID articleId, UUID requestUserId) {
        ArticleDto result = queryFactory
                .select(Projections.constructor(
                        ArticleDto.class,
                        a.id,
                        a.source,
                        a.sourceUrl,
                        a.title,
                        a.publishDate,
                        a.summary,
                        a.commentCount,
                        a.viewCount,
                        new CaseBuilder()
                                .when(av.id.isNotNull())
                                .then(true)
                                .otherwise(false)
                ))
                .from(a)
                .leftJoin(av)
                .on(
                        av.articleId.eq(a.id),
                        av.userId.eq(requestUserId)
                )
                .where(
                        a.id.eq(articleId),
                        a.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public CursorPageResponseArticle searchArticles(UUID cursor, Instant after, int size, ArticleSearchRequest request) {
        BooleanBuilder where = new BooleanBuilder();
        boolean isDesc = "desc".equals(request.getDirection());

        where.and(a.isDeleted.isFalse()); // 논리 삭제 안된 기사만 조회

        String keyword = request.getKeyword();;
        if (keyword != null && !keyword.isBlank()) {
            where.and(a.title.containsIgnoreCase(keyword)
                    .or(a.summary.containsIgnoreCase(keyword)));
        }

        if (request.getInterestId() != null) {
            where.and(
                    JPAExpressions
                            .selectOne()
                            .from(ai)
                            .where(
                                    ai.articleId.eq(a.id),
                                    ai.interestId.eq(request.getInterestId())
                            )
                            .exists()
            );
        }

        if (request.getSourceIn() != null && !request.getSourceIn().isEmpty()) {
            where.and(a.source.in(request.getSourceIn()));
        }

        if (request.getPublishDateFrom() != null) {
            where.and(a.publishDate.goe(request.getPublishDateFrom()));
        }

        if (request.getPublishDateTo() != null) {
            where.and(a.publishDate.loe(request.getPublishDateTo()));
        }

        if (cursor != null && after != null) {
            switch (request.getOrderBy()) {

                case "publishDate":
                    if (isDesc) {
                        where.and(a.publishDate.lt(after)
                                        .or(a.publishDate.eq(after)
                                                        .and(a.id.lt(cursor))
                                        )
                        );
                    } else {
                        where.and(a.publishDate.gt(after)
                                        .or(a.publishDate.eq(after)
                                                        .and(a.id.gt(cursor))
                                        )
                        );
                    }
                    break;

                case "viewCount":
                    // TODO : 보조커서 다시 잡아서 만들어야 함
                    break;

                case "commentCount":
                    // TODO : 보조커서 다시 잡아서 만들어야 함
                    break;

                default:
                    if (isDesc) {
                        where.and(a.id.lt(cursor));
                    } else {
                        where.and(a.id.gt(cursor));
                    }
                    break;
            }
        }

        return null;
    }

    @Override
    public List<String> findSource() {
        return List.of();
    }
}
