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
        validateSimilarInterestName(request.getName());

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

    private void validateSimilarInterestName(String newName) {
        List<Interest> interests = interestRepository.findAll();

        boolean existsSimilarName = interests.stream()
                .anyMatch(interest -> isSimilarName(interest.getName(), newName));

        if (existsSimilarName) {
            throw new BaseException(ErrorCode.INTEREST_SIMILAR_NAME_EXISTS);
        }
    }

    private boolean isSimilarName(String existingName, String newName) { // 동일 관심사 및 유사도 80퍼
        String a = normalize(existingName);
        String b = normalize(newName);

        if (a.equals(b)) {
            return true;
        }

        double similarity = calculateSimilarity(a, b);

        if (Math.max(a.length(), b.length()) <= 4) { // 4글자 이하일때는 75퍼
            return similarity >= 0.75;
        }

        return similarity >= 0.8;
    }

    private double calculateSimilarity(String a, String b) { // 레벤슈타인 이용한 유사도 측정
        int distance = levenshteinDistance(a, b);
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    private String normalize(String value) { // 문자열 정규화
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private int levenshteinDistance(String a, String b) { // 레벤슈타인 거리 -> 유사도 측정
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

}
