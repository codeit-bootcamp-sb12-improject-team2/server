package com.codeit.server.useractivity.service;

import com.codeit.server.useractivity.dto.UserActivityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

  @Override
  public UserActivityDto getUserActivity(String userId) {
    return null;
  }
}
