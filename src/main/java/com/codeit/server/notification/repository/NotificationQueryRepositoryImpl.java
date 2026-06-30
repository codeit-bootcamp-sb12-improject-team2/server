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
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

  private final MongoTemplate mongoTemplate;

  // 1. 피드백 반영하여 수정한 커서 조회 메서드
  @Override
  public List<Notification> findUnconfirmedNotificationsByCursor(
      UUID userId, String cursorId, Instant afterInstant, int limit) {



    Query query = new Query();

    Criteria baseCriteria = Criteria.where("user_id").is(userId)
        .and("confirmed").is(false);

    // 💡 수정된 커서 페이징 조건 처리 (방어벽 해제 및 유연화)
    if (cursorId != null && !cursorId.isBlank()) {
      try {
        UUID targetUuid = UUID.fromString(cursorId);

        if (afterInstant != null) {
          // 1. 날짜(afterInstant)와 ID(cursorId) 둘 다 조합해서 스크롤 내릴 때
          Criteria cursorCriteria = new Criteria().orOperator(
              Criteria.where("createdAt").lt(afterInstant),
              Criteria.where("createdAt").is(afterInstant).and("_id").lt(targetUuid)
          );
          baseCriteria.andOperator(cursorCriteria);
        } else {
          // 2. 혹시라도 날짜 없이 ID(cursorId)만 넘어왔을 때 안전하게 ID 기반으로만 페이징 처리
          baseCriteria.and("_id").lt(targetUuid);
        }
      } catch (IllegalArgumentException e) {
        // 잘못된 UUID 포맷 방어 코드
      }
    }

    query.addCriteria(baseCriteria);
    query.with(Sort.by(Sort.Direction.DESC, "createdAt", "_id"));
    query.limit(limit + 1);

    return mongoTemplate.find(query, Notification.class);
  }

  // 2. 기존 유지하는 카운트 메서드
  @Override
  public long countUnconfirmedByUserId(UUID userId) {
    Query query = new Query(
        Criteria.where("user_id").is(userId)
            .and("confirmed").is(false)
    );
    return mongoTemplate.count(query, Notification.class);
  }

  // 3. 기존 유지하는 단건 상태 수정 메서드
  @Override
  public void updateConfirmStatus(UUID notificationId, boolean confirmed) {
    Query query = new Query(Criteria.where("_id").is(notificationId));
    Update update = new Update().set("confirmed", confirmed).set("updatedAt", Instant.now());
    mongoTemplate.updateFirst(query, update, Notification.class);
  }

  // 4. 기존 유지하는 전체 상태 수정 메서드
  @Override
  public void updateAllConfirmStatusByUserId(UUID userId, boolean confirmed) {
    Query query = new Query(Criteria.where("user_id").is(userId).and("confirmed").is(!confirmed));
    Update update = new Update().set("confirmed", confirmed).set("updatedAt", Instant.now());
    mongoTemplate.updateMulti(query, update, Notification.class);
  }

  // 5. 기존 유지하는 배치 삭제 메서드
  @Override
  public void deleteConfirmedNotificationsOlderThan(Instant thresholdDate) {
    Query query = new Query(
        Criteria.where("confirmed").is(true)
            .and("updatedAt").lt(thresholdDate)
    );
    remove(query); // 몽고디비 remove 실행
  }

  // 내부 헬퍼 메서드 (기존 remove 호출 대응용)
  private void remove(Query query) {
    mongoTemplate.remove(query, Notification.class);
  }
}