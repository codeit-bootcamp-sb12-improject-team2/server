package com.codeit.server.interest.entity;


import com.codeit.server.global.common.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Interest extends BaseUpdatableEntity {

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "subscriber_count")
    private Integer subscriberCount;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestKeyword> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "interest")
    private List<Subscription> subscriptions = new ArrayList<>();

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