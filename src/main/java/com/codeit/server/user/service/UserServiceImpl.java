package com.codeit.server.user.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.user.dto.UserDto;
import com.codeit.server.user.dto.UserLoginRequest;
import com.codeit.server.user.dto.UserRegisterRequest;
import com.codeit.server.user.dto.UserUpdateRequest;
import com.codeit.server.user.entity.User;
import com.codeit.server.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto register(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new BaseException(ErrorCode.USER_ALREADY_EXISTS);
    }

    if (userRepository.existsByNickname(request.nickname())) {
      throw new BaseException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(request.password());
    User user = User.create(request.email(), request.nickname(), encodedPassword);

    User savedUser = userRepository.save(user);
    log.info("회원가입 완료: userId={}", savedUser.getId());

    return UserDto.from(savedUser);
  }

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByEmailAndIsDeletedFalse(request.email())
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BaseException(ErrorCode.INVALID_CREDENTIALS);
    }

    return UserDto.from(user);
  }

  @Override
  @Transactional
  public UserDto update(UUID userId, UUID requestUserId, UserUpdateRequest request) {
    validateUserAccess(userId, requestUserId);

    User user = findActiveUser(userId);

    if (user.getNickname().equals(request.nickname())) {
      return UserDto.from(user);
    }

    if (userRepository.existsByNickname(request.nickname())) {
      throw new BaseException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
    }

    user.update(request.nickname());
    log.info("회원정보 수정 완료: userId={}", userId);
    return UserDto.from(user);
  }

  @Override
  @Transactional
  public void delete(UUID userId, UUID requestUserId) {
    validateUserAccess(userId, requestUserId);

    User user = findActiveUser(userId);
    user.delete();

    log.info("사용자 논리 삭제 완료: userId={}", userId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID userId, UUID requestUserId) {
    validateUserAccess(userId, requestUserId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

    userRepository.delete(user);

    log.info("사용자 물리 삭제 완료: userId={}", userId);
  }

  @Override
  @Transactional
  public void purgeExpiredSoftDeletedUsers() {
    Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);

    List<User> expiredUsers = userRepository.findAllSoftDeletedBefore(threshold);

    userRepository.deleteAll(expiredUsers);

    log.info("사용자 물리 삭제 완료: {}명", expiredUsers.size());
  }

  private User findActiveUser(UUID userId) {
    return userRepository.findByIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
  }

  private void validateUserAccess(UUID userId, UUID requestUserId) {
    if (!requestUserId.equals(userId)) {
      throw new BaseException(ErrorCode.USER_ACCESS_DENIED);
    }
  }
}

