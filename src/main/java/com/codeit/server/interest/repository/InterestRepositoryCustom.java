package com.codeit.server.interest.repository;

import com.codeit.server.interest.entity.Interest;

import java.util.List;
import java.util.UUID;

public interface InterestRepositoryCustom {

    List<Interest> searchWithCursor(
            String keyword,
            String orderBy,
            String cursor,
            String nextAfter,
            int limit,
            UUID userId
    );

    long countByKeyword(String keyword);
}
