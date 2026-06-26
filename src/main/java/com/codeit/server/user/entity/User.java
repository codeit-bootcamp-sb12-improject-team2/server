package com.codeit.server.user.entity;

import com.codeit.server.global.common.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class User extends BaseUpdatableEntity {

  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "nickname", nullable = false, length = 50)
  private String nickname;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;

  public static User create(String email, String nickname, String encodedPassword) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(encodedPassword)
        .isDeleted(false)
        .build();
  }

  public void update(String nickname) {
    this.nickname = nickname;
  }

  public void delete() {
    this.isDeleted = true;
  }
}
