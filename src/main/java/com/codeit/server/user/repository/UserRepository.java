package com.codeit.server.user.repository;

import com.codeit.server.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Optional<User> findByEmailAndIsDeletedFalse(String email);

  Optional<User> findByIdAndIsDeletedFalse(UUID id);

  @Query("SELECT u FROM User u WHERE u.isDeleted = true AND u.updatedAt < :threshold")
  List<User> findAllSoftDeletedBefore(@Param("threshold") Instant threshold);
}
