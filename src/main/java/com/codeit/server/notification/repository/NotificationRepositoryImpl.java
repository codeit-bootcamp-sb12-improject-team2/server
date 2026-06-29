package com.codeit.server.notification.repository;

import com.codeit.server.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationQueryRepository {

  private final MongoTemplate mongoTemplate;

  public List<Notification> findUnconfirmedNotificationsByCursor(
      UUID userId, UUID cursorId, Instant afterInstant, int limit) {

    Query query = new Query();

    // 1. 기본 필터: 내 알림이면서 아직 읽지 않은 알림(confirmed == false)
    Criteria criteria = Criteria.where("user_id").is(userId)
        .and("confirmed").is(false);

    // 2. 복합 커서 페이징 조건 (최신순 정렬 기준)
    if (afterInstant != null && cursorId != null) {
      criteria.andOperator(
          new Criteria().orOperator(
              // 조건 A: 보조 커서(시간)가 기준 시간보다 더 과거인 데이터
              Criteria.where("createdAt").lt(afterInstant),
              // 조건 B: 시간이 소수점 밀리초까지 같을 경우, 고유 ID(UUID) 기준 역순으로 중복 방지
              Criteria.where("createdAt").is(afterInstant).and("id").lt(cursorId)
          )
      );
    }

    query.addCriteria(criteria);

    // 3. 정렬 조건: 최신순 (1순위: createdAt 내림차순, 2순위: id 내림차순)
    query.with(Sort.by(Sort.Direction.DESC, "createdAt", "id"));

    // 4. 다음 페이지 유무 확인을 위해 한 개 더(+1) 조회
    query.limit(limit + 1);

    return mongoTemplate.find(query, Notification.class);
  }

  @Override
  public long countUnconfirmedByUserId(UUID userId) {
    Query query = new Query(
        Criteria.where("user_id").is(userId)
            .and("confirmed").is(false)
    );
    return mongoTemplate.count(query, Notification.class);
  }
}

