package com.codeit.server.interest;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.interest.dto.CursorPageResponse;
import com.codeit.server.interest.dto.InterestCreateRequest;
import com.codeit.server.interest.dto.InterestResponse;
import com.codeit.server.interest.dto.InterestUpdateRequest;
import com.codeit.server.interest.entity.Interest;
import com.codeit.server.interest.entity.Subscription;
import com.codeit.server.interest.repository.InterestKeywordRepository;
import com.codeit.server.interest.repository.InterestRepository;
import com.codeit.server.interest.repository.SubscriptionRepository;
import com.codeit.server.interest.service.InterestService;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.isNull;

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

    private UUID userId;
    private UUID interestId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        interestId = UUID.randomUUID();
        user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
    }

    private Interest buildInterest(UUID id, String name, int subscriberCount) {
        Interest interest = Interest.builder()
                .name(name)
                .subscriberCount(subscriberCount)
                .keywords(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(interest, "id", id);
        ReflectionTestUtils.setField(interest, "createdAt", Instant.now());
        ReflectionTestUtils.setField(interest, "updatedAt", Instant.now());
        return interest;
    }

    private InterestCreateRequest buildCreateRequest(String name, List<String> keywords) {
        InterestCreateRequest request = new InterestCreateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "keywords", keywords);
        return request;
    }

    private InterestUpdateRequest buildUpdateRequest(String name, List<String> keywords) {
        InterestUpdateRequest request = new InterestUpdateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "keywords", keywords);
        return request;
    }

    private void stubSaveWithTimestamps() {
        given(interestRepository.save(any(Interest.class))).willAnswer(invocation -> {
            Interest saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(saved, "createdAt", Instant.now());
            ReflectionTestUtils.setField(saved, "updatedAt", Instant.now());
            return saved;
        });
    }

    @Nested
    @DisplayName("관심사 생성")
    class Create {

        @Test
        @DisplayName("이름이 유사하면 INTEREST_SIMILAR_NAME_EXISTS 예외가 발생한다")
        void throwsException_whenNameAlreadyExists() {
            InterestCreateRequest request = buildCreateRequest("test1", List.of("test1"));
            Interest existing = buildInterest(UUID.randomUUID(), "test1", 0);
            given(interestRepository.findAll()).willReturn(List.of(existing));

            assertThatThrownBy(() -> interestService.create(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_SIMILAR_NAME_EXISTS);

            verify(interestRepository, never()).save(any());
        }

        @Test
        @DisplayName("이름이 중복되지 않고 키워드가 있으면 관심사와 키워드를 저장한다")
        void savesInterestAndKeywords_whenValid() {
            InterestCreateRequest request = buildCreateRequest("Elon", List.of("Tesla", "Elon_1"));
            given(interestRepository.findAll()).willReturn(List.of());
            stubSaveWithTimestamps();

            InterestResponse response = interestService.create(request);

            verify(interestRepository).save(any(Interest.class));
            verify(interestKeywordRepository).saveAll(anyList());
            assertThat(response.getName()).isEqualTo("Elon");
        }

        @Test
        @DisplayName("키워드가 null이면 saveAll을 호출하지 않고 관심사만 저장한다")
        void savesInterestWithoutKeywords_whenKeywordsNull() {
            InterestCreateRequest request = buildCreateRequest("엔비디아", null);
            given(interestRepository.findAll()).willReturn(List.of());
            stubSaveWithTimestamps();

            interestService.create(request);

            verify(interestRepository).save(any(Interest.class));
            verify(interestKeywordRepository, never()).saveAll(anyList());
        }
    }


    @Nested
    @DisplayName("관심사 검색")
    class Search {

        @Test
        @DisplayName("repository가 size + 1개를 반환하면 hasNext는 true이고 nextCursor/nextAfter가 설정된다")
        void returnsHasNextTrue_whenMoreItemsThanSize() {
            int size = 9;
            List<Interest> tenItems = new ArrayList<>();
            for (int i = 0; i < size + 1; i++) {
                tenItems.add(buildInterest(UUID.randomUUID(), "관심사" + i, i));
            }

            given(interestRepository.searchWithCursor(
                    isNull(), isNull(), isNull(), isNull(), isNull(), eq(size + 1), eq(userId)
            )).willReturn(tenItems);
            given(interestRepository.countByKeyword(null)).willReturn(100L);
            given(subscriptionRepository.findAllByUserId(userId)).willReturn(List.of());

            CursorPageResponse<InterestResponse> result =
                    interestService.search(null, null, null, null, null, size, userId);

            assertThat(result.getContent()).hasSize(size);
            assertThat(result.isHasNext()).isTrue();
            assertThat(result.getNextCursor()).isNotNull();
            assertThat(result.getNextAfter()).isNotNull();
        }

        @Test
        @DisplayName("repository가 정확히 size개를 반환하면 hasNext는 false이고 커서 값은 null이다")
        void returnsHasNextFalse_whenExactSize() {
            int size = 9;
            List<Interest> nineItems = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                nineItems.add(buildInterest(UUID.randomUUID(), "관심사" + i, i));
            }

            given(interestRepository.searchWithCursor(
                    isNull(), isNull(), isNull(), isNull(), isNull(), eq(size + 1), eq(userId)
            )).willReturn(nineItems);
            given(interestRepository.countByKeyword(null)).willReturn(9L);
            given(subscriptionRepository.findAllByUserId(userId)).willReturn(List.of());

            CursorPageResponse<InterestResponse> result =
                    interestService.search(null, null, null, null, null, size, userId);

            assertThat(result.getContent()).hasSize(size);
            assertThat(result.isHasNext()).isFalse();
            assertThat(result.getNextCursor()).isNull();
            assertThat(result.getNextAfter()).isNull();
        }

        @Test
        @DisplayName("orderBy가 subscriberCount이면 nextCursor를 구독자 수 기준으로 계산한다")
        void resolvesNextCursor_bySubscriberCount_whenOrderByIsSubscriberCount() {
            int size = 1;
            Interest first = buildInterest(UUID.randomUUID(), "인공지능", 24);
            Interest extra = buildInterest(UUID.randomUUID(), "생성형AI", 11);

            given(interestRepository.searchWithCursor(
                    isNull(), eq("subscriberCount"), isNull(), isNull(), isNull(), eq(size + 1), eq(userId)
            )).willReturn(List.of(first, extra));
            given(interestRepository.countByKeyword(null)).willReturn(2L);
            given(subscriptionRepository.findAllByUserId(userId)).willReturn(List.of());

            CursorPageResponse<InterestResponse> result =
                    interestService.search(null, "subscriberCount", null, null, null, size, userId);

            assertThat(result.getNextCursor()).isEqualTo("24");
        }

        @Test
        @DisplayName("userId가 기존 구독을 가지고 있으면 해당 관심사를 구독 중으로 표시한다")
        void marksSubscribedInterests_whenUserIdProvided() {
            Interest subscribed = buildInterest(interestId, "챔피언스리그", 2);

            given(interestRepository.searchWithCursor(
                    isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), eq(userId)
            )).willReturn(List.of(subscribed));
            given(interestRepository.countByKeyword(null)).willReturn(1L);

            Subscription subscription = mock(Subscription.class);
            given(subscription.getInterest()).willReturn(subscribed);
            given(subscriptionRepository.findAllByUserId(userId)).willReturn(List.of(subscription));

            CursorPageResponse<InterestResponse> result =
                    interestService.search(null, null, null, null, null, 10, userId);

            assertThat(result.getContent().get(0).getSubscribedByMe()).isTrue();
        }

        @Test
        @DisplayName("userId가 null이면 구독 정보를 조회하지 않는다")
        void skipsSubscriptionLookup_whenUserIdNull() {
            given(interestRepository.searchWithCursor(
                    isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), isNull()
            )).willReturn(List.of());
            given(interestRepository.countByKeyword(null)).willReturn(0L);

            interestService.search(null, null, null, null, null, 10, null);

            verify(subscriptionRepository, never()).findAllByUserId(any());
        }
    }

    @Nested
    @DisplayName("관심사 수정")
    class Update {

        @Test
        @DisplayName("관심사가 존재하지 않으면 INTEREST_NOT_FOUND 예외가 발생한다")
        void throwsException_whenInterestNotFound() {
            given(interestRepository.findById(interestId)).willReturn(Optional.empty());
            InterestUpdateRequest request = buildUpdateRequest("새 이름", List.of("키워드"));

            assertThatThrownBy(() -> interestService.update(interestId, request, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }

        @Test
        @DisplayName("이름과 키워드가 모두 주어지면 이름을 변경하고 키워드를 교체한다")
        void updatesNameAndKeywords_whenValid() {
            Interest interest = buildInterest(interestId, "이전 이름", 5);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)).willReturn(true);

            InterestUpdateRequest request = buildUpdateRequest("새 이름", List.of("키워드1", "키워드2"));

            InterestResponse response = interestService.update(interestId, request, userId);

            assertThat(response.getName()).isEqualTo("새 이름");
            assertThat(response.getSubscribedByMe()).isTrue();
            verify(interestRepository).flush();
        }

        @Test
        @DisplayName("이름이 공백이면 이름을 변경하지 않는다")
        void doesNotRename_whenNameBlank() {
            Interest interest = buildInterest(interestId, "기존 이름", 5);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

            InterestUpdateRequest request = buildUpdateRequest("   ", null);

            InterestResponse response = interestService.update(interestId, request, userId);

            assertThat(response.getName()).isEqualTo("기존 이름");
        }
    }

    @Nested
    @DisplayName("관심사 구독")
    class Subscribe {

        @Test
        @DisplayName("구독을 생성하고 구독자 수를 증가시킨다")
        void createsSubscriptionAndIncrementsCount() {
            Interest interest = buildInterest(interestId, "관심사", 0);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionRepository.existsByUserAndInterest(user, interest)).willReturn(false);

            InterestResponse response = interestService.subscribe(interestId, userId);

            verify(subscriptionRepository).save(any(Subscription.class));
            assertThat(response.getSubscriberCount()).isEqualTo(1);
            assertThat(response.getSubscribedByMe()).isTrue();
        }

        @Test
        @DisplayName("이미 구독 중이면 ALREADY_SUBSCRIBED 예외가 발생한다")
        void throwsException_whenAlreadySubscribed() {
            Interest interest = buildInterest(interestId, "관심사", 3);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionRepository.existsByUserAndInterest(user, interest)).willReturn(true);

            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_SUBSCRIBED);

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 INTEREST_NOT_FOUND 예외가 발생한다")
        void throwsException_whenInterestNotFound() {
            given(interestRepository.findById(interestId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            Interest interest = buildInterest(interestId, "관심사", 0);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("관심사 구독 취소")
    class Unsubscribe {

        @Test
        @DisplayName("구독을 삭제하고 구독자 수를 감소시킨다")
        void deletesSubscriptionAndDecrementsCount() {
            Interest interest = buildInterest(interestId, "관심사", 5);
            Subscription subscription = mock(Subscription.class);

            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionRepository.findByUserAndInterest(user, interest))
                    .willReturn(Optional.of(subscription));

            interestService.unsubscribe(interestId, userId);

            verify(subscriptionRepository).delete(subscription);
            assertThat(interest.getSubscriberCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("구독이 존재하지 않으면 SUBSCRIPTION_NOT_FOUND 예외가 발생한다")
        void throwsException_whenSubscriptionNotFound() {
            Interest interest = buildInterest(interestId, "관심사", 5);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(subscriptionRepository.findByUserAndInterest(user, interest))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.unsubscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBSCRIPTION_NOT_FOUND);

            verify(subscriptionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 INTEREST_NOT_FOUND 예외가 발생한다")
        void throwsException_whenInterestNotFound() {
            given(interestRepository.findById(interestId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.unsubscribe(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("관심사 완전 삭제")
    class HardDelete {

        @Test
        @DisplayName("관심사가 존재하면 삭제한다")
        void deletesInterest_whenFound() {
            Interest interest = buildInterest(interestId, "관심사", 0);
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

            interestService.hardDelete(interestId, userId);

            verify(interestRepository).delete(interest);
        }

        @Test
        @DisplayName("관심사가 존재하지 않으면 INTEREST_NOT_FOUND 예외가 발생한다")
        void throwsException_whenNotFound() {
            given(interestRepository.findById(interestId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.hardDelete(interestId, userId))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTEREST_NOT_FOUND);

            verify(interestRepository, never()).delete(any());
        }
    }
}