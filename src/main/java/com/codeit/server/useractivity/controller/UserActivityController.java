package com.codeit.server.useractivity.controller;


import com.codeit.server.useractivity.dto.UserActivityDto;
import com.codeit.server.useractivity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

  private final UserActivityService userActivityService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserActivityDto> getUserActivity(
      @PathVariable String userId
  ) {
    UserActivityDto activity = userActivityService.getUserActivity(userId);

    return ResponseEntity.ok(activity);
  }

}
