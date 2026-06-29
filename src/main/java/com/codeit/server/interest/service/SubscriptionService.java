package com.codeit.server.interest.service;

import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.SubscriptionResponse;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.interest.repository.InterestRepository;
import com.codeit.server.interest.repository.SubscriptionRepository;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;

    // Subscribe a user to an interest
    @Transactional
    public SubscriptionResponse subscribe(UUID userId, UUID interestId) {
        if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
            throw new CustomException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND));

        Subscription subscription = Subscription.builder()
                .user(user)
                .interest(interest)
                .build();

        interest.increaseSubscriberCount();
        subscriptionRepository.save(subscription);

        return SubscriptionResponse.from(subscription);
    }

    // Unsubscribe a user from an interest
    @Transactional
    public void unsubscribe(UUID userId, UUID interestId) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndInterestId(userId, interestId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.getInterest().decreaseSubscriberCount();
        subscriptionRepository.delete(subscription);
    }

    // Get the list of subscriptions for a specific user (my page)
    public Page<SubscriptionResponse> findByUser(UUID userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
                .map(SubscriptionResponse::from);
    }

    // Get the list of users subscribed to a specific interest (e.g. for notification targeting)
    public List<SubscriptionResponse> findSubscribersByInterest(UUID interestId) {
        return subscriptionRepository.findByInterestId(interestId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    // Check whether a user is subscribed to an interest
    public boolean isSubscribed(UUID userId, UUID interestId) {
        return subscriptionRepository.existsByUserIdAndInterestId(userId, interestId);
    }

    // Get subscriber count for an interest (used for verification/sync)
    public long countSubscribers(UUID interestId) {
        return subscriptionRepository.countByInterestId(interestId);
    }
}