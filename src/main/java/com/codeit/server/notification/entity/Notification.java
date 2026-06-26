package com.codeit.server.notification.entity;


import com.codeit.server.global.common.BaseUpdatableEntity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collation = "notifications")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

  @Id
  private UUID id;

  @Field("user_id")
  private UUID userId;

  private String content;

  @Builder.Default
  private boolean confirmed = false;

  @Field("resource_type")
  private String resourceType;

  @Field("resource_id")
  private UUID resourceId;

}
