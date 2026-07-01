package com.codeit.server.interest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interest_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class InterestKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(name = "keyword", length = 30, nullable = false)
    private String keyword;

    void assignInterest(Interest interest) {
        this.interest = interest;
    }
}