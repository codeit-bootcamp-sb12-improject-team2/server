package com.codeit.server.rank.repository;

import com.codeit.server.rank.entity.ArticleRanking;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleRankingRepository extends JpaRepository<ArticleRanking, UUID> {
    @Transactional
    void deleteByRankingDate(LocalDate rankingDate);

    List<ArticleRanking> findByRankingDateAndRankTypeOrderByRankingAsc(LocalDate rankingDate, String rankType);
}
