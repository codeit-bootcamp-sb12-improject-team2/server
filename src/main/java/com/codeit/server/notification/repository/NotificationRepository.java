package com.codeit.server.notification.repository;

import com.codeit.server.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository
  extends MongoRepository<Notification, UUID>, NotificationQueryRepository{

}
