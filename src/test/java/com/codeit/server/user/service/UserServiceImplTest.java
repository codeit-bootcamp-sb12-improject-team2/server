package com.codeit.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.user.dto.UserDto;
import com.codeit.server.user.dto.UserLoginRequest;
import com.codeit.server.user.dto.UserRegisterRequest;
import com.codeit.server.user.dto.UserUpdateRequest;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  @DisplayName("회원가입 성공")
  void register_success() {
    UserRegisterRequest request = new UserRegisterRequest("testUser", "test@example.com", "password123");
    User savedUser = user(UUID.randomUUID(), request.email(), request.nickname(), false);

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByNickname(request.nickname())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserDto result = userService.register(request);

    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void register_fail_duplicateEmail() {
    UserRegisterRequest request = new UserRegisterRequest("testUser", "test@example.com", "password123");

    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("회원가입 실패 - 닉네임 중복")
  void register_fail_duplicateNickname() {
    UserRegisterRequest request = new UserRegisterRequest("testUser", "test@example.com", "password123");

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(userRepository.existsByNickname(request.nickname())).thenReturn(true);

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    UserLoginRequest request = new UserLoginRequest("test@example.com", "password123");
    User user = user(UUID.randomUUID(), request.email(), "testUser", false);

    when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

    UserDto result = userService.login(request);

    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("로그인 실패 - 존재하지 않는 이메일")
  void login_fail_userNotFound() {
    UserLoginRequest request = new UserLoginRequest("unknown@example.com", "password123");

    when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_fail_wrongPassword() {
    UserLoginRequest request = new UserLoginRequest("test@example.com", "wrongPassword");
    User user = user(UUID.randomUUID(), request.email(), "testUser", false);

    when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("회원정보 수정 성공")
  void update_success() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedUser");
    User user = user(userId, "test@example.com", "testUser", false);

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByNickname(request.nickname())).thenReturn(false);

    UserDto result = userService.update(userId, userId, request);

    assertThat(result.nickname()).isEqualTo("updatedUser");
  }

  @Test
  @DisplayName("회원정보 수정 성공 - 요청자 ID가 없으면 대상 ID를 사용한다")
  void update_success_requestUserIdNull() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedUser");
    User user = user(userId, "test@example.com", "testUser", false);

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByNickname(request.nickname())).thenReturn(false);

    UserDto result = userService.update(userId, null, request);

    assertThat(result.nickname()).isEqualTo("updatedUser");
  }

  @Test
  @DisplayName("회원정보 수정 성공 - 기존 닉네임과 같으면 그대로 반환")
  void update_success_sameNickname() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("testUser");
    User user = user(userId, "test@example.com", "testUser", false);

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

    UserDto result = userService.update(userId, userId, request);

    assertThat(result.nickname()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("회원정보 수정 실패 - 권한 없음")
  void update_fail_accessDenied() {
    UUID userId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedUser");

    assertThatThrownBy(() -> userService.update(userId, requestUserId, request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ACCESS_DENIED);
  }

  @Test
  @DisplayName("회원정보 수정 실패 - 사용자 없음")
  void update_fail_userNotFound() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedUser");

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, userId, request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("회원정보 수정 실패 - 닉네임 중복")
  void update_fail_duplicateNickname() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("duplicatedUser");
    User user = user(userId, "test@example.com", "testUser", false);

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByNickname(request.nickname())).thenReturn(true);

    assertThatThrownBy(() -> userService.update(userId, userId, request))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("회원 논리 삭제 성공")
  void delete_success() {
    UUID userId = UUID.randomUUID();
    User user = user(userId, "test@example.com", "testUser", false);

    when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

    userService.delete(userId, userId);

    assertThat(user.isDeleted()).isTrue();
  }

  @Test
  @DisplayName("회원 물리 삭제 성공")
  void hardDelete_success() {
    UUID userId = UUID.randomUUID();
    User user = user(userId, "test@example.com", "testUser", true);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.hardDelete(userId, userId);

    verify(userRepository).delete(user);
  }

  @Test
  @DisplayName("회원 물리 삭제 실패 - 사용자 없음")
  void hardDelete_fail_userNotFound() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.hardDelete(userId, userId))
        .isInstanceOf(BaseException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
  }

  private User user(UUID id, String email, String nickname, boolean isDeleted) {
    return User.builder()
        .id(id)
        .email(email)
        .nickname(nickname)
        .password("encodedPassword")
        .isDeleted(isDeleted)
        .build();
  }
}
