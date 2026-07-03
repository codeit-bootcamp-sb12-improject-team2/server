package com.codeit.server.interest.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.CursorPageResponse;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
import com.codeit.server.interest.dto.InterestUpdateRequest;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.InterestKeyword;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import com.codeit.server.interest.repository.InterestRepository;
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
public class InterestService {

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
            String cursor,
            String nextAfter,
            int size,
            UUID userId
    ) {
        List<Interest> interests = interestRepository.searchWithCursor(
                keyword, orderBy, cursor, nextAfter, size + 1, userId
        );

        boolean hasNext = interests.size() > size;
        if (hasNext) interests = interests.subList(0, size);

        String nextCursor = null;
        String newNextAfter = null;

        if (hasNext) {
            Interest last = interests.get(interests.size() - 1);
            nextCursor = resolveNextCursor(last, orderBy);
            newNextAfter = last.getId().toString();
        }

        long totalElements = interestRepository.countByKeyword(keyword);

        return CursorPageResponse.of(
                interests.stream().map(InterestResponse::from).toList(),
                nextCursor,
                newNextAfter,
                size,
                totalElements
        );
    }
    
        // Get interests subscribed to by a specific user (dashboard / my page)
    public Page<InterestResponse> findSubscribedInterests(UUID userId, Pageable pageable) {
        return interestRepository.findSubscribedInterestsByUserId(userId, pageable)
                .map(InterestResponse::from);
    }

    // Update interest name and keywords
    @Transactional
    public InterestResponse update(UUID interestId, InterestUpdateRequest request, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        interest.rename(request.getName());  // updateName() → rename()

        return InterestResponse.from(interest);
    }

    // Subscribe to an interest
    @Transactional
    public InterestResponse subscribe(UUID interestId, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        interest.increaseSubscriberCount();  // incrementSubscriberCount() → increaseSubscriberCount()

        return InterestResponse.from(interest);
    }


    // Unsubscribe from an interest
    @Transactional
    public void unsubscribe(UUID interestId, UUID userId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTEREST_NOT_FOUND));

        interest.decreaseSubscriberCount();  // decrementSubscriberCount() → decreaseSubscriberCount()
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
        return switch (orderBy.toUpperCase()) {
            case "SUBSCRIBER" -> String.valueOf(interest.getSubscriberCount());
            default -> interest.getName();  // NAME or default
        };
    }
}
