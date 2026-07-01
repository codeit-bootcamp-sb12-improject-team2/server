package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.QInterest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInterest interest = QInterest.interest;

    @Override
    public List<Interest> searchWithCursor(
            String keyword,
            String orderBy,
            String cursor,
            String nextAfter,
            int limit,
            UUID userId  // reserved for subscription status enrichment
    ) {
        return queryFactory
                .selectFrom(interest)
                .where(
                        keywordContains(keyword),
                        cursorCondition(orderBy, cursor, nextAfter)
                )
                .orderBy(orderByCondition(orderBy))
                .limit(limit)
                .fetch();
    }

    @Override
    public long countByKeyword(String keyword) {
        Long count = queryFactory
                .select(interest.count())
                .from(interest)
                .where(keywordContains(keyword))
                .fetchOne();
        return count != null ? count : 0L;
    }

    // Keyword filter: case-insensitive name match
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return interest.name.containsIgnoreCase(keyword);
    }

    // Cursor predicate
    private BooleanExpression cursorCondition(String orderBy, String cursor, String nextAfter) {
        if (cursor == null) return null;

        UUID afterId = nextAfter != null ? UUID.fromString(nextAfter) : null;

        if (orderBy == null) return nameAfterCursor(cursor, afterId);

        return switch (orderBy.toUpperCase()) {
            case "SUBSCRIBER" -> subscriberAfterCursor(cursor, afterId);
            default -> nameAfterCursor(cursor, afterId);
        };
    }

    // Subscriber sort is DESC; tie-break by id ASC
    private BooleanExpression subscriberAfterCursor(String cursor, UUID afterId) {
        int cursorCount = Integer.parseInt(cursor);

        if (afterId == null) {
            return interest.subscriberCount.lt(cursorCount);
        }

        return interest.subscriberCount.lt(cursorCount)
                .or(interest.subscriberCount.eq(cursorCount)
                        .and(interest.id.lt(afterId)));  //
    }

    // Name sort is ASC; tie-break by id ASC
    private BooleanExpression nameAfterCursor(String cursor, UUID afterId) {
        if (afterId == null) {
            return interest.name.gt(cursor);
        }

        return interest.name.gt(cursor)
                .or(interest.name.eq(cursor)
                        .and(interest.id.gt(afterId)));  //
    }

    // Order specifier: SUBSCRIBER → desc by count, default → asc by name
    private OrderSpecifier<?> orderByCondition(String orderBy) {
        if (orderBy == null) return interest.name.asc();

        return switch (orderBy.toUpperCase()) {
            case "SUBSCRIBER" -> interest.subscriberCount.desc();
            default -> interest.name.asc();
        };
    }
}