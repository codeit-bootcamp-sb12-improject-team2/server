package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.QInterest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
            String direction,
            String cursor,
            String after,
            int limit,
            UUID userId  // reserved for subscription status enrichment
    ) {
        return queryFactory
                .selectFrom(interest)
                .where(
                        keywordContains(keyword),
                        cursorCondition(orderBy, direction, cursor, after)
                )
                .orderBy(
                        orderByCondition(orderBy, direction),
                        createdAtOrderCondition(direction),
                        idOrderCondition(direction) // 보조 정렬
                )
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
    private BooleanExpression cursorCondition(String orderBy, String direction, String cursor, String after) {
        if (cursor == null) return null;

        boolean isDesc = "DESC".equalsIgnoreCase(direction);

        // 수정: Swagger의 after는 date-time이므로 UUID가 아니라 Instant로 파싱
        Instant afterInstant = null;
        if (after != null && !after.isBlank()) {
            afterInstant = Instant.parse(after);
        }

        if ("subscriberCount".equalsIgnoreCase(orderBy)) {
            return subscriberCursorCondition(cursor, afterInstant, isDesc);
        }

        // 기본 정렬은 name
        return nameCursorCondition(cursor, afterInstant, isDesc);
    }

    // subscriber sort
    private BooleanExpression subscriberCursorCondition(
            String cursor,
            Instant after,
            boolean isDesc
    ) {
        int cursorCount = Integer.parseInt(cursor);

        // 수정: direction에 따라 lt / gt 비교 방향 변경
        if (after == null) {
            return isDesc
                    ? interest.subscriberCount.lt(cursorCount)
                    : interest.subscriberCount.gt(cursorCount);
        }

        // 수정: subscriberCount가 같은 경우 createdAt(after)로 보조 커서 처리
        return isDesc
                ? interest.subscriberCount.lt(cursorCount)
                .or(
                        interest.subscriberCount.eq(cursorCount)
                                .and(interest.createdAt.lt(after))
                )
                : interest.subscriberCount.gt(cursorCount)
                .or(
                        interest.subscriberCount.eq(cursorCount)
                                .and(interest.createdAt.gt(after))
                );
    }

    // Name sort
    private BooleanExpression nameCursorCondition(
            String cursor,
            Instant after,
            boolean isDesc
    ) {
        // 수정: direction에 따라 name 비교 방향 변경
        if (after == null) {
            return isDesc
                    ? interest.name.lt(cursor)
                    : interest.name.gt(cursor);
        }

        // 수정: name이 같은 경우 createdAt(after)로 보조 커서 처리
        return isDesc
                ? interest.name.lt(cursor)
                .or(
                        interest.name.eq(cursor)
                                .and(interest.createdAt.lt(after))
                )
                : interest.name.gt(cursor)
                .or(
                        interest.name.eq(cursor)
                                .and(interest.createdAt.gt(after))
                );
    }

    // Order specifier -> direction 반영 했어요
    private OrderSpecifier<?> orderByCondition(String orderBy, String direction) {
        boolean isDesc = "DESC".equalsIgnoreCase(direction);
        // 수정: toUpperCase() + "subscriberCount" 비교 버그 제거
        if ("subscriberCount".equalsIgnoreCase(orderBy)) {
            return isDesc
                    ? interest.subscriberCount.desc()
                    : interest.subscriberCount.asc();
        }

        return isDesc
                ? interest.name.desc()
                : interest.name.asc();
    }

    private OrderSpecifier<?> createdAtOrderCondition(String direction) {
        boolean isDesc = "DESC".equalsIgnoreCase(direction);

        return isDesc
                ? interest.createdAt.desc()
                : interest.createdAt.asc();
    }

    // tie breaker by id입니다
    private OrderSpecifier<?> idOrderCondition(String direction) {
        boolean isDesc = "DESC".equalsIgnoreCase(direction);

        // 수정: 같은 name/subscriberCount가 여러 개 있을 때 순서가 흔들리지 않게 보조 정렬 추가
        return isDesc
                ? interest.id.desc()
                : interest.id.asc();
    }
}