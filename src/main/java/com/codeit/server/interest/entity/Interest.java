package com.codeit.server.interest.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Interest {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "subscriber_count")
    private Integer subscriberCount; // NULL 허용

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestKeyword> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "interest")
    private List<Subscription> subscriptions = new ArrayList<>();

    @Builder
    public Interest(String name) {
        this.name = name;
        this.subscriberCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addKeyword(InterestKeyword keyword) {
        keywords.add(keyword);
        keyword.assignInterest(this);
    }

    public void increaseSubscriberCount() {
        this.subscriberCount = (this.subscriberCount == null ? 0 : this.subscriberCount) + 1;
    }

    public void decreaseSubscriberCount() {
        if (this.subscriberCount != null && this.subscriberCount > 0) {
            this.subscriberCount -= 1;
        }
    }

    public void rename(String name) {
        this.name = name;
    }
}