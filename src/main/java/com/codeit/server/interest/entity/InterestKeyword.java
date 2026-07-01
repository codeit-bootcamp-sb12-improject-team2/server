package com.codeit.server.interest.entity;


import com.codeit.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "interest_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class InterestKeyword extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(name = "keyword", length = 30, nullable = false)
    private String keyword;

    void assignInterest(Interest interest) {
        this.interest = interest;
    }
}