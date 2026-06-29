package com.codeit.server.interest.service;

import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
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
            throw new CustomException(ErrorCode.DUPLICATE_INTEREST_NAME);
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

    // retrieve a single interest in an ID.
    public InterestResponse findById(UUID id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND));
        return InterestResponse.from(interest);
    }

    // Search interests by name keyword (paged)
    public Page<InterestResponse> search(String keyword, Pageable pageable) {
        Page<Interest> interests = (keyword == null || keyword.isBlank())
                ? interestRepository.findAll(pageable)
                : interestRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return interests.map(InterestResponse::from);
    }

    // Get most popular interests ordered by subscriber count
    public Page<InterestResponse> findPopularInterests(Pageable pageable) {
        return interestRepository.findAllOrderBySubscriberCountDesc(pageable)
                .map(InterestResponse::from);
    }

    // Get interests subscribed to by a specific user (dashboard / my page)
    public Page<InterestResponse> findSubscribedInterests(UUID userId, Pageable pageable) {
        return interestRepository.findSubscribedInterestsByUserId(userId, pageable)
                .map(InterestResponse::from);
    }

    // Update interest keywords (replace all)
    @Transactional
    public InterestResponse updateKeywords(UUID interestId, List<String> keywords) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND));

        interestKeywordRepository.deleteByInterestId(interestId);

        for (String keyword : keywords) {
            InterestKeyword interestKeyword = InterestKeyword.builder()
                    .interest(interest)
                    .keyword(keyword)
                    .build();
            interestKeywordRepository.save(interestKeyword);
        }

        return InterestResponse.from(interest);
    }

    // Delete an interest
    @Transactional
    public void delete(UUID id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND));
        interestRepository.delete(interest);
    }
}