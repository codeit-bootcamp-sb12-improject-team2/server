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
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

  private final MongoTemplate mongoTemplate;

  public List<Notification> findUnconfirmedNotificationsByCursor(
      UUID userId, UUID cursorId, Instant afterInstant, int limit) {

    Query query = new Query();

    Criteria baseCriteria = Criteria.where("user_id").is(userId)
        .and("confirmed").is(false);

    if (afterInstant != null && cursorId != null) {
      Criteria cursorCriteria = new Criteria().orOperator(
          Criteria.where("createdAt").lt(afterInstant),
          Criteria.where("createdAt").is(afterInstant).and("_id").lt(cursorId)
      );

      baseCriteria.andOperator(cursorCriteria);
    }

    query.addCriteria(baseCriteria);

    query.with(Sort.by(Sort.Direction.DESC, "createdAt", "_id"));
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