package com.codeit.server.useractivity.service;

import com.codeit.server.global.exception.BaseException;
import com.codeit.server.global.exception.ErrorCode;
import com.codeit.server.useractivity.dto.UserActivityDto;
import com.codeit.server.useractivity.repository.UserActivityQueryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

  private final UserActivityQueryRepository userActivityQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public UserActivityDto getUserActivity(String userId) {

    UUID userUuid;
    try{
      userUuid = UUID.fromString(userId);
    } catch (IllegalArgumentException e){
      throw new BaseException(ErrorCode.INVALID_REQUEST);
    }

    UserActivityDto.UserActivityDtoBuilder builder =userActivityQueryRepository.findUserAndSubscriptions(userUuid)
        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

    builder.comments(userActivityQueryRepository.findRecentCommentsByUserId(userUuid, 10));
    builder.commentLikes(userActivityQueryRepository.findRecentCommentLikesByUserId(userUuid, 10));
    builder.articleViews(userActivityQueryRepository.findRecentArticleViewsByUserId(userUuid, 10));

    return builder.build();
  }
}
