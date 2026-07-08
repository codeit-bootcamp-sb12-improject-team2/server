package com.codeit.server.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.CursorPageResponse;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
import com.codeit.server.interest.dto.InterestUpdateRequest;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.InterestKeyword;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import com.codeit.server.interest.repository.InterestRepository;
import com.codeit.server.interest.repository.SubscriptionRepository;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private InterestKeywordRepository interestKeywordRepository;

    @InjectMocks
    private InterestService interestService;

    private Interest createInterest(UUID id, String name, int subscriberCount) {
        return Interest.builder()
                .id(id)
                .name(name)
                .subscriberCount(subscriberCount)
                .keywords(new ArrayList<>())
                .createdAt(Instant.parse("2026-07-08T00:00:00Z"))
                .updatedAt(Instant.parse("2026-07-08T00:00:00Z"))
                .build();
    }

    private User createUser(UUID id, String nickname, String email) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .password("password")
                .isDeleted(false)
                .build();
    }

    @Nested
    @DisplayName("Create Interest Tests")
    class CreateInterestTest {

        @Test
        @DisplayName("성공 - 신규 관심사를 키워드와 함께 저장한다")
        void create_success() {
            // Given
            InterestCreateRequest request = InterestCreateRequest.builder()
                    .name("Spring Boot")
                    .keywords(List.of("Java", "Web", "Backend"))
                    .build();

            UUID interestId = UUID.randomUUID();
            Interest savedInterest = createInterest(interestId, "Spring Boot", 0);

            when(interestRepository.findAll()).thenReturn(Collections.emptyList());
            when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
                Interest input = invocation.getArgument(0);
                ReflectionTestUtils.setField(input, "id", interestId);
                ReflectionTestUtils.setField(input, "createdAt", Instant.now());
                ReflectionTestUtils.setField(input, "updatedAt", Instant.now());
                return input;
            });

            // When
            InterestResponse response = interestService.create(request);

            // Then
            assertThat(response.getName()).isEqualTo("Spring Boot");
            assertThat(response.getKeywords()).containsExactlyInAnyOrder("Java", "Web", "Backend");
            assertThat(response.getSubscriberCount()).isEqualTo(0);
            verify(interestRepository).save(any(Interest.class));
            verify(interestKeywordRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("실패 - 기존 관심사 이름과 동일하거나 정규화 시 같으면 예외를 반환한다")
        void create_fail_exactDuplicateOrNormalizedNameExists() {
            // Given
            InterestCreateRequest request = InterestCreateRequest.builder()
                    .name("SpringBoot")
                    .build();

            Interest existing = createInterest(UUID.randomUUID(), "Spring Boot", 3);
            when(interestRepository.findAll()).thenReturn(List.of(existing));

            // When & Then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_SIMILAR_NAME_EXISTS);
        }

        @Test
        @DisplayName("실패 - 유사도 80% 이상(5글자 이상인 경우) 유사한 이름의 관심사가 존재하면 예외를 반환한다")
        void create_fail_similarNameExists_longName() {
            // Given
            // "Python" (6글자) 과 "Pythn" (5글자) -> levenshtein distance: 1. Max length: 6.
            // similarity: 1 - 1/6 = 0.833 >= 0.8
            InterestCreateRequest request = InterestCreateRequest.builder()
                    .name("Pythn")
                    .build();

            Interest existing = createInterest(UUID.randomUUID(), "Python", 5);
            when(interestRepository.findAll()).thenReturn(List.of(existing));

            // When & Then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_SIMILAR_NAME_EXISTS);
        }

        @Test
        @DisplayName("실패 - 유사도 75% 이상(4글자 이하인 경우) 유사한 이름의 관심사가 존재하면 예외를 반환한다")
        void create_fail_similarNameExists_shortName() {
            // Given
            // "Rust" (4글자) 와 "Rst" (3글자) -> levenshtein distance: 1. Max length: 4.
            // similarity: 1 - 1/4 = 0.75 >= 0.75
            InterestCreateRequest request = InterestCreateRequest.builder()
                    .name("Rst")
                    .build();

            Interest existing = createInterest(UUID.randomUUID(), "Rust", 5);
            when(interestRepository.findAll()).thenReturn(List.of(existing));

            // When & Then
            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_SIMILAR_NAME_EXISTS);
        }

        @Test
        @DisplayName("성공 - 유사하지 않은 이름이면 등록할 수 있다")
        void create_success_nonSimilarName() {
            // Given
            // "Go" (2글자) 와 "G" (1글자) -> levenshtein distance: 1. Max length: 2.
            // similarity: 1 - 1/2 = 0.5 < 0.75
            InterestCreateRequest request = InterestCreateRequest.builder()
                    .name("G")
                    .keywords(Collections.emptyList())
                    .build();

            Interest existing = createInterest(UUID.randomUUID(), "Go", 10);
            when(interestRepository.findAll()).thenReturn(List.of(existing));
            when(interestRepository.save(any(Interest.class))).thenAnswer(invocation -> {
                Interest input = invocation.getArgument(0);
                ReflectionTestUtils.setField(input, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(input, "createdAt", Instant.now());
                ReflectionTestUtils.setField(input, "updatedAt", Instant.now());
                return input;
            });

            // When
            InterestResponse response = interestService.create(request);

            // Then
            assertThat(response.getName()).isEqualTo("G");
        }
    }

    @Nested
    @DisplayName("Search Interests Tests")
    class SearchInterestsTest {

        @Test
        @DisplayName("성공 - 커서 기반 검색 결과를 반환하고 다음 페이지 정보를 제공한다")
        void search_success_withNextPage() {
            // Given
            UUID userId = UUID.randomUUID();
            String keyword = "java";
            String orderBy = "subscriberCount";
            String direction = "DESC";
            String cursor = "10";
            String after = "2026-07-08T00:00:00Z";
            int size = 2;

            Interest interest1 = createInterest(UUID.randomUUID(), "Java Core", 8);
            Interest interest2 = createInterest(UUID.randomUUID(), "Spring Web", 5);
            Interest interest3 = createInterest(UUID.randomUUID(), "JPA Advanced", 3);

            // searchWithCursor size + 1 (= 3) 호출 시 3개 반환하도록 모의
            when(interestRepository.searchWithCursor(keyword, orderBy, direction, cursor, after, size + 1, userId))
                    .thenReturn(List.of(interest1, interest2, interest3));
            when(interestRepository.countByKeyword(keyword)).thenReturn(15L);

            Subscription sub = Subscription.builder()
                    .user(createUser(userId, "nick", "e@mail.com"))
                    .interest(interest1)
                    .build();
            when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of(sub));

            // When
            CursorPageResponse<InterestResponse> result =
                    interestService.search(keyword, orderBy, direction, cursor, after, size, userId);

            // Then
            assertThat(result.getContent()).hasSize(size);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Java Core");
            assertThat(result.getContent().get(0).getSubscribedByMe()).isTrue();
            assertThat(result.getContent().get(1).getName()).isEqualTo("Spring Web");
            assertThat(result.getContent().get(1).getSubscribedByMe()).isFalse();

            assertThat(result.isHasNext()).isTrue();
            // nextCursor should be subscriberCount of the last item in subList (interest2 has 5 subscriberCount)
            assertThat(result.getNextCursor()).isEqualTo("5");
            assertThat(result.getNextAfter()).isEqualTo(interest2.getCreatedAt().toString());
            assertThat(result.getTotalElements()).isEqualTo(15L);
        }

        @Test
        @DisplayName("성공 - 로그인 하지 않은 유저의 검색 시 subscribedByMe가 항상 false로 채워진다")
        void search_success_anonymousUser() {
            // Given
            String keyword = "java";
            int size = 5;

            Interest interest = createInterest(UUID.randomUUID(), "Java Core", 8);
            when(interestRepository.searchWithCursor(keyword, null, null, null, null, size + 1, null))
                    .thenReturn(List.of(interest));
            when(interestRepository.countByKeyword(keyword)).thenReturn(1L);

            // When
            CursorPageResponse<InterestResponse> result =
                    interestService.search(keyword, null, null, null, null, size, null);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSubscribedByMe()).isFalse();
            verifyNoInteractions(subscriptionRepository);
        }
    }

    @Nested
    @DisplayName("Find Subscribed Interests Tests")
    class FindSubscribedInterestsTest {

        @Test
        @DisplayName("성공 - 유저가 구독한 관심사를 페이징하여 가져온다")
        void findSubscribedInterests_success() {
            // Given
            UUID userId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Interest interest = createInterest(UUID.randomUUID(), "Java", 2);

            when(interestRepository.findSubscribedInterestsByUserId(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(interest), pageable, 1));

            // When
            Page<InterestResponse> result = interestService.findSubscribedInterests(userId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Java");
            assertThat(result.getContent().get(0).getSubscribedByMe()).isTrue();
        }
    }

    @Nested
    @DisplayName("Update Interest Tests")
    class UpdateInterestTest {

        @Test
        @DisplayName("성공 - 관심사의 이름과 키워드를 수정한다")
        void update_success() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            InterestUpdateRequest request = InterestUpdateRequest.builder()
                    .name("Updated Spring")
                    .keywords(List.of("Reactive", "Cloud"))
                    .build();

            Interest interest = createInterest(interestId, "Spring Boot", 2);

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)).thenReturn(true);

            // When
            InterestResponse response = interestService.update(interestId, request, userId);

            // Then
            assertThat(response.getName()).isEqualTo("Updated Spring");
            assertThat(response.getKeywords()).containsExactlyInAnyOrder("Reactive", "Cloud");
            assertThat(response.getSubscribedByMe()).isTrue();
            verify(interestRepository).flush();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 관심사를 수정하려 하면 예외를 반환한다")
        void update_fail_notFound() {
            // Given
            UUID interestId = UUID.randomUUID();
            InterestUpdateRequest request = InterestUpdateRequest.builder().name("New Name").build();

            when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> interestService.update(interestId, request, null))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Subscribe Tests")
    class SubscribeTest {

        @Test
        @DisplayName("성공 - 유저가 관심사를 새로 구독하고 구독수가 증가한다")
        void subscribe_success() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = createInterest(interestId, "AWS", 2);
            User user = createUser(userId, "user1", "user1@example.com");

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(subscriptionRepository.existsByUserAndInterest(user, interest)).thenReturn(false);

            // When
            InterestResponse response = interestService.subscribe(interestId, userId);

            // Then
            assertThat(response.getSubscriberCount()).isEqualTo(3);
            assertThat(response.getSubscribedByMe()).isTrue();
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("실패 - 이미 구독중인 관심사에 재구독을 시도하면 예외를 반환한다")
        void subscribe_fail_alreadySubscribed() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = createInterest(interestId, "AWS", 2);
            User user = createUser(userId, "user1", "user1@example.com");

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(subscriptionRepository.existsByUserAndInterest(user, interest)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_SUBSCRIBED);
            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 관심사를 구독하려 하면 예외를 반환한다")
        void subscribe_fail_interestNotFound() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자가 구독하려 하면 예외를 반환한다")
        void subscribe_fail_userNotFound() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = createInterest(interestId, "AWS", 2);
            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Unsubscribe Tests")
    class UnsubscribeTest {

        @Test
        @DisplayName("성공 - 구독을 취소하고 구독자 수를 감소시킨다")
        void unsubscribe_success() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = createInterest(interestId, "AWS", 2);
            User user = createUser(userId, "user1", "user1@example.com");
            Subscription sub = Subscription.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .interest(interest)
                    .build();

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserAndInterest(user, interest)).thenReturn(Optional.of(sub));

            // When
            interestService.unsubscribe(interestId, userId);

            // Then
            assertThat(interest.getSubscriberCount()).isEqualTo(1);
            verify(subscriptionRepository).delete(sub);
        }

        @Test
        @DisplayName("실패 - 구독 관계가 없는데 취소를 시도하면 예외를 반환한다")
        void unsubscribe_fail_subscriptionNotFound() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            Interest interest = createInterest(interestId, "AWS", 2);
            User user = createUser(userId, "user1", "user1@example.com");

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserAndInterest(user, interest)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> interestService.unsubscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBSCRIPTION_NOT_FOUND);
            verify(subscriptionRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Hard Delete Tests")
    class HardDeleteTest {

        @Test
        @DisplayName("성공 - 관심사를 삭제한다")
        void hardDelete_success() {
            // Given
            UUID interestId = UUID.randomUUID();
            Interest interest = createInterest(interestId, "AWS", 2);

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

            // When
            interestService.hardDelete(interestId, null);

            // Then
            verify(interestRepository).delete(interest);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 관심사를 삭제하려 하면 예외를 반환한다")
        void hardDelete_fail_notFound() {
            // Given
            UUID interestId = UUID.randomUUID();
            when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> interestService.hardDelete(interestId, null))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }
    }
}
