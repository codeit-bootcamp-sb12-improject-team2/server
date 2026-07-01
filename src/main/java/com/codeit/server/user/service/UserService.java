package com.codeit.server.user.service;

import com.codeit.server.user.dto.UserDto;
import com.codeit.server.user.dto.UserLoginRequest;
import com.codeit.server.user.dto.UserRegisterRequest;
import com.codeit.server.user.dto.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

  UserDto register(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto update(UUID userId, UUID requestUserId, UserUpdateRequest request);

  void delete(UUID userId, UUID requestUserId);

  void hardDelete(UUID userId, UUID requestUserId);
}
