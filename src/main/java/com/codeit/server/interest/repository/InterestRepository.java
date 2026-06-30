package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Interest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    // duplicate name check
    boolean existsByName(String name);

    Optional<Interest> findByName(String name);

    // keyword name search (case-insensitive)
    Page<Interest> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Query sorted by subscriber count (descending)
    @Query("SELECT i FROM Interest i ORDER BY i.subscriberCount DESC")
    Page<Interest> findAllOrderBySubscriberCountDesc(Pageable pageable);

    // Retrieve the list of interests subscribed to by a specific user
    // for dashboard/ my page
    @Query("""
            SELECT i FROM Interest i
            JOIN Subscription s ON s.interest = i
            WHERE s.user.id = :userId
            """)
    Page<Interest> findSubscribedInterestsByUserId(@Param("userId") UUID userId, Pageable pageable);
}