package com.codeit.server.interest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "interest_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestKeyword {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(name = "keyword", length = 30, nullable = false)
    private String keyword;

    @Builder
    public InterestKeyword(Interest interest, String keyword) {
        this.interest = interest;
        this.keyword = keyword;
    }

    // 연관관계 편의 메서드 (Interest.addKeyword에서 사용)
    void assignInterest(Interest interest) {
        this.interest = interest;
    }
}