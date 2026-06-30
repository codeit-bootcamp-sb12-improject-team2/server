package com.codeit.server.user.controller;

import com.codeit.server.user.dto.UserDto;
import com.codeit.server.user.dto.UserLoginRequest;
import com.codeit.server.user.dto.UserRegisterRequest;
import com.codeit.server.user.dto.UserUpdateRequest;
import com.codeit.server.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
    return userService.register(request);
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public UserDto login(@Valid @RequestBody UserLoginRequest request) {
    return userService.login(request);
  }

  @PatchMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto update(
      @PathVariable UUID userId,
      @RequestHeader("MoNew-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    return userService.update(userId, requestUserId, request);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @RequestHeader("MoNew-Request-User-ID") UUID requestUserId,
      @PathVariable UUID userId
  ) {
    userService.delete(userId, requestUserId);
  }

  @DeleteMapping("/{userId}/hard")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void hardDelete(
      @RequestHeader("MoNew-Request-User-ID") UUID requestUserId,
      @PathVariable UUID userId
  ) {
    userService.hardDelete(userId, requestUserId);
  }
}
