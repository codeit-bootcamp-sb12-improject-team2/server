package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.InterestKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

    // Find all keyword included in specific interests
    List<InterestKeyword> findByInterestId(UUID interestId);

    // Find by Keyword text (can be applied on interest recommendation/matching )
    List<InterestKeyword> findByKeywordContainingIgnoreCase(String keyword);

    // Delete all keywords of a specific interest (used when replacing keywords on interest update)
    void deleteByInterestId(UUID interestId);

    //duplicate keyword registration check
    boolean existsByInterestIdAndKeyword(UUID interestId, String keyword);
}