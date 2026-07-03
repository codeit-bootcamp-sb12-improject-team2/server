package com.codeit.server.interest.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.CursorPageResponse;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
import com.codeit.server.interest.dto.InterestUpdateRequest;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.InterestKeyword;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import com.codeit.server.interest.repository.InterestRepository;
import com.codeit.server.interest.repository.SubscriptionRepository;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final InterestKeywordRepository interestKeywordRepository;

    // Create a new interest along with its keywords
    @Transactional
    public InterestResponse create(InterestCreateRequest request) {
        if (interestRepository.existsByName(request.getName())) {
            throw new BaseException(ErrorCode.INTEREST_ALREADY_EXISTS);
        }

        Interest interest = Interest.builder()
                .name(request.getName())
                .subscriberCount(0)
                .keywords(new ArrayList<>())
                .build();
        interestRepository.save(interest);

        if (request.getKeywords() != null) {
            for (String keyword : request.getKeywords()) {
                InterestKeyword interestKeyword = InterestKeyword.builder()
                        .interest(interest)
                        .keyword(keyword)
                        .build();
                interest.addKeyword(interestKeyword);
            }
            interestKeywordRepository.saveAll(interest.getKeywords());
        }

        return InterestResponse.from(interest);
    }

    // Search interests by keyword with cursor-based pagination
    public CursorPageResponse<InterestResponse> search(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            String nextAfter,
            int size,
            UUID userId
    ) {
        List<Interest> interests = interestRepository.searchWithCursor(
                keyword, orderBy, direction, cursor, nextAfter, size + 1, userId
        );

        boolean hasNext = interests.size() > size;
        if (hasNext) interests = interests.subList(0, size);

        String nextCursor = null;
        String newNextAfter = null;

        if (hasNext) {
            Interest last = interests.get(interests.size() - 1);
            nextCursor = resolveNextCursor(last, orderBy);
            newNextAfter = last.getCreatedAt().toString(); // UUID date-time
        }

        long totalElements = interestRepository.countByKeyword(keyword);

        java.util.Set<UUID> subscribedInterestIds = (userId != null)
                ? subscriptionRepository.findAllByUserId(userId).stream()
                        .map(sub -> sub.getInterest().getId())
                        .collect(java.util.stream.Collectors.toSet())
                : java.util.Collections.emptySet();

        return CursorPageResponse.of(
                interests.stream()
                        .map(interest -> InterestResponse.from(interest, subscribedInterestIds.contains(interest.getId())))
                        .toList(),
                nextCursor,
                newNextAfter,
                size,
                totalElements
        );
    }
    
        // Get interests subscribed to by a specific user (dashboard / my page)
    public Page<InterestResponse> findSubscribedInterests(UUID userId, Pageable pageable) {
        return interestRepository.findSubscribedInterestsByUserId(userId, pageable)
                .map(interest -> InterestResponse.from(interest, true));
    }

    @Transactional
    public InterestResponse update(UUID interestId, InterestUpdateRequest request, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            interest.rename(request.getName());
        }

        if (request.getKeywords() != null) {
            interest.getKeywords().clear();

            interestRepository.flush();

            for (String keyword : request.getKeywords()) {
                InterestKeyword interestKeyword = InterestKeyword.builder()
                        .keyword(keyword)
                        .build();

                interest.addKeyword(interestKeyword);
            }
        }

        boolean isSubscribed =
                (userId != null) && subscriptionRepository.existsByUserIdAndInterestId(userId, interestId);

        return InterestResponse.from(interest, isSubscribed);
    }

    @Transactional
    public InterestResponse subscribe(UUID interestId, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (subscriptionRepository.existsByUserAndInterest(user, interest)) {
            throw new BaseException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .interest(interest)
                .build();

        subscriptionRepository.save(subscription);

        interest.increaseSubscriberCount();

        return InterestResponse.from(interest, true);
    }


    @Transactional
    public void unsubscribe(UUID interestId, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Subscription subscription = subscriptionRepository
                .findByUserAndInterest(user, interest)
                .orElseThrow(() -> new BaseException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscriptionRepository.delete(subscription);

        interest.decreaseSubscriberCount();
    }

    // Hard delete an interest
    @Transactional
    public void hardDelete(UUID interestId, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));
        interestRepository.delete(interest);
    }

    // Resolve nextCursor value based on orderBy
    private String resolveNextCursor(Interest interest, String orderBy) {
        if (orderBy == null) return interest.getName();
        return switch (orderBy) {
            case "subscriberCount" -> String.valueOf(interest.getSubscriberCount()); // subscriberCount로 변수명 수정
            default -> interest.getName();  // NAME or default
        };
    }
}
