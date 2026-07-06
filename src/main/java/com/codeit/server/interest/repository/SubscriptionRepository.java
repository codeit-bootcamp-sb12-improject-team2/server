package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // Check whether a subscription exists (used when toggling subscribe/unsubscribe)
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);

    List<Subscription> findAllByUserId(UUID userId);

    boolean existsByUserAndInterest(User user, Interest interest);

    Optional<Subscription> findByUserAndInterest(User user, Interest interest);

}