package com.codeit.server.interest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.server.global.config.JpaAuditingConfig;
import com.codeit.server.global.config.QuerydslConfig;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ActiveProfiles("test")
@DataJpaTest(properties = {
    "spring.datasource.url=${DB_URL}",
    "spring.datasource.username=${DB_USERNAME}",
    "spring.datasource.password=${DB_PASSWORD}",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.database=postgresql",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.data.mongodb.uri=${MONGO_URI}",
    "spring.data.mongodb.database=monew",
    "spring.data.mongodb.uuid-representation=standard"
})
@Import({QuerydslConfig.class, InterestRepositoryImpl.class, JpaAuditingConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InterestRepositoryTest {

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // Generate a unique prefix for this test run to prevent conflict in the shared RDS
    private final String prefix = "T" + UUID.randomUUID().toString().substring(0, 4) + "_";

    private Interest createAndSaveInterest(String name, int subscriberCount) {
        Interest interest = Interest.builder()
                .name(prefix + name)
                .subscriberCount(subscriberCount)
                .build();
        return interestRepository.save(interest);
    }

    @Nested
    @DisplayName("Basic JPA Methods & Custom Subscription Queries")
    class BasicQueryTest {

        @Test
        @DisplayName("성공 - name으로 관심사가 존재하는지 여부를 확인하고 조회한다")
        void success_existsByNameAndFindByName() {
            // Given
            createAndSaveInterest("Vue.js", 3);

            // When
            boolean exists = interestRepository.existsByName(prefix + "Vue.js");
            boolean notExists = interestRepository.existsByName(prefix + "React");
            Optional<Interest> found = interestRepository.findByName(prefix + "Vue.js");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo(prefix + "Vue.js");
        }

        @Test
        @DisplayName("성공 - 특정 유저가 구독한 관심사들만 페이징 조회한다")
        void success_findSubscribedInterestsByUserId() {
            // Given
            String uniqueEmail = "user_" + UUID.randomUUID() + "@example.com";
            User user1 = userRepository.save(User.create(uniqueEmail, "user1", "pw"));
            String uniqueEmail2 = "user_" + UUID.randomUUID() + "@example.com";
            User user2 = userRepository.save(User.create(uniqueEmail2, "user2", "pw"));

            Interest interest1 = createAndSaveInterest("Kubernetes", 1);
            Interest interest2 = createAndSaveInterest("Docker", 5);
            Interest interest3 = createAndSaveInterest("Jenkins", 2);

            subscriptionRepository.save(Subscription.builder().user(user1).interest(interest1).build());
            subscriptionRepository.save(Subscription.builder().user(user1).interest(interest3).build());
            subscriptionRepository.save(Subscription.builder().user(user2).interest(interest2).build());

            // When
            Page<Interest> page = interestRepository.findSubscribedInterestsByUserId(user1.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                    .extracting(Interest::getName)
                    .containsExactlyInAnyOrder(prefix + "Kubernetes", prefix + "Jenkins");
        }
    }

    @Nested
    @DisplayName("searchWithCursor Custom Querydsl Tests")
    class SearchWithCursorTest {

        @Test
        @DisplayName("성공 - 키워드 필터링이 올바르게 적용된다")
        void success_filterByKeyword() {
            // Given
            createAndSaveInterest("Spring Core", 10);
            createAndSaveInterest("Spring Web", 5);
            createAndSaveInterest("Django Framework", 2);

            // When
            // prefix를 함께 넘겨 우리 테스트 데이터로 한정하여 spring을 검색
            List<Interest> result = interestRepository.searchWithCursor(prefix + "spring", null, null, null, null, 10, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Interest::getName)
                    .containsExactlyInAnyOrder(prefix + "Spring Core", prefix + "Spring Web");
        }

        @Test
        @DisplayName("성공 - 구독자 수 기준 내림차순(DESC) 정렬 및 커서 페이징이 작동한다")
        void success_orderBySubscriberCountDesc_withCursor() {
            // Given
            Interest int1 = createAndSaveInterest("React", 15); // 1st
            Interest int2 = createAndSaveInterest("Next.js", 10); // 2nd
            Interest int3 = createAndSaveInterest("Svelte", 5); // 3rd
            Interest int4 = createAndSaveInterest("Angular", 2); // 4th

            // 1. First Page (size = 2, orderBy subscriberCount DESC)
            // prefix를 검색하여 다른 DB 데이터 노이즈 제거
            List<Interest> firstPage = interestRepository.searchWithCursor(
                    prefix, "subscriberCount", "DESC", null, null, 2, null
            );
            assertThat(firstPage).hasSize(2);
            assertThat(firstPage.get(0).getName()).isEqualTo(prefix + "React");
            assertThat(firstPage.get(1).getName()).isEqualTo(prefix + "Next.js");

            // 2. Next Page (cursor = 10, nextAfter = Next.js's createdAt)
            String cursorValue = String.valueOf(firstPage.get(1).getSubscriberCount());
            String nextAfterValue = firstPage.get(1).getCreatedAt().toString();

            List<Interest> secondPage = interestRepository.searchWithCursor(
                    prefix, "subscriberCount", "DESC", cursorValue, nextAfterValue, 2, null
            );

            // Then
            assertThat(secondPage).hasSize(2);
            assertThat(secondPage.get(0).getName()).isEqualTo(prefix + "Svelte");
            assertThat(secondPage.get(1).getName()).isEqualTo(prefix + "Angular");
        }

        @Test
        @DisplayName("성공 - 관심사 이름 기준 오름차순(ASC) 정렬 및 커서 페이징이 작동한다")
        void success_orderByNameAsc_withCursor() {
            // Given
            createAndSaveInterest("Apple", 1);
            createAndSaveInterest("Banana", 2);
            createAndSaveInterest("Cherry", 3);

            // First Page
            List<Interest> firstPage = interestRepository.searchWithCursor(
                    prefix, "name", "ASC", null, null, 2, null
            );
            assertThat(firstPage).hasSize(2);
            assertThat(firstPage.get(0).getName()).isEqualTo(prefix + "Apple");
            assertThat(firstPage.get(1).getName()).isEqualTo(prefix + "Banana");

            // Second Page (cursor = "Banana", nextAfter = Banana's createdAt)
            String cursorValue = firstPage.get(1).getName();
            String nextAfterValue = firstPage.get(1).getCreatedAt().toString();

            List<Interest> secondPage = interestRepository.searchWithCursor(
                    prefix, "name", "ASC", cursorValue, nextAfterValue, 2, null
            );

            // Then
            assertThat(secondPage).hasSize(1);
            assertThat(secondPage.get(0).getName()).isEqualTo(prefix + "Cherry");
        }

        @Test
        @DisplayName("성공 - 키워드 매칭 개수가 정확히 카운트된다")
        void success_countByKeyword() {
            // Given
            createAndSaveInterest("Kubernetes", 1);
            createAndSaveInterest("Knative", 2);
            createAndSaveInterest("Docker", 3);

            // When
            // prefix + 'k'를 키워드로 넘겨 다른 테스트 데이터와 간섭을 방지
            long count = interestRepository.countByKeyword(prefix + "k");

            // Then
            assertThat(count).isEqualTo(2);
        }
    }
}
