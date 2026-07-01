package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // Check whether a subscription exists (used when toggling subscribe/unsubscribe)
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);

    // Retrieve a single subscription (used when unsubscribing)
    Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);

    // Retrieve the list of subscriptions for a specific user (my page)
    Page<Subscription> findByUserId(UUID userId, Pageable pageable);

    // Retrieve the list of users subscribed to a specific interest (e.g. for notification targeting)
    List<Subscription> findByInterestId(UUID interestId);

    // Bulk delete subscriptions when an interest is deleted
    void deleteByInterestId(UUID interestId);

    // Bulk delete subscriptions when a user is deactivated/deleted
    void deleteByUserId(UUID userId);

    // Count subscribers of a specific interest (used to sync/verify subscriberCount)
    long countByInterestId(UUID interestId);
}