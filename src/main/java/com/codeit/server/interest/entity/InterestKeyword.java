package com.codeit.server.interest.entity;

import jakarta.persistence.*;
import lombok.*;
import com.codeit.server.global.common.BaseEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

@Entity
@Table(name = "interest_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class InterestKeyword {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(name = "keyword", length = 30, nullable = false)
    private String keyword;

    @PrePersist
    void init() {
        if (this.id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    void assignInterest(Interest interest) {
        this.interest = interest;
    }
}