package com.codeit.server.useractivity.service;

import com.codeit.server.useractivity.dto.UserActivityDto;

public interface UserActivityService {

  UserActivityDto getUserActivity(String userId);

}
