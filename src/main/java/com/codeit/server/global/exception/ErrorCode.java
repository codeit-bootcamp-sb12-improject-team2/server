package com.codeit.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


  // 1. User 도메인 (사용자 관리 / 로그인)

  USER_NOT_FOUND("존재하지 않는 사용자입니다."),
  USER_ALREADY_EXISTS("이미 가입된 사용자 아이디(이메일)입니다."),
  INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다."),
  USER_ALREADY_DELETED("이미 논리 삭제(탈퇴)된 사용자입니다."),

  // 2. Article 도메인 (뉴스 기사 관리)

  ARTICLE_NOT_FOUND("존재하지 않는 뉴스 기사입니다."),
  ARTICLE_ALREADY_DELETED("이미 삭제된 뉴스 기사입니다."),
  ARTICLE_NOT_DELETED("삭제되지 않은 기사는 복구할 수 없습니다."),
  ARTICLE_VIEW_RECORD_FAILED("기사 조회수 등록에 실패했습니다."),


  // 3. Notification 도메인 (알림 관리)

  NOTIFICATION_NOT_FOUND("존재하지 않는 알림입니다."),
  NOTIFICATION_ACCESS_DENIED("해당 알림에 대한 접근 권한이 없습니다."),


  // 4. Interest 도메인 (관심사 / 구독 관리)

  INTEREST_NOT_FOUND("존재하지 않는 관심사 카테고리입니다."),
  INTEREST_ALREADY_EXISTS("이미 존재하는 관심사 이름입니다."),
  ALREADY_SUBSCRIBED("이미 구독 중인 관심사입니다."),
  SUBSCRIPTION_NOT_FOUND("구독 내역을 찾을 수 없습니다."),

  // 4-1. Interest Keyword 도메인 ( 관심사 키워드 관리)

  DUPLICATE_KEYWORD( "이미 등록된 키워드입니다."),

  // 5. Comment 도메인 (댓글 / 좋아요 관리)

  COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),
  COMMENT_ACCESS_DENIED("댓글을 수정/삭제할 권한이 없습니다."),
  COMMENT_ALREADY_DELETED("이미 삭제된 댓글입니다."),
  ALREADY_LIKED_COMMENT("이미 좋아요를 누른 댓글입니다."),
  COMMENT_LIKE_NOT_FOUND("좋아요 내역이 존재하지 않아 취소할 수 없습니다."),


  // 6. Common & Global (기본 예외 공통)

  INVALID_REQUEST("잘못된 요청 파라미터 또는 문법입니다."),
  VALIDATION_ERROR("요청 데이터 유효성 검사에 실패하였습니다."),
  TYPE_MISMATCH("요청 파라미터의 데이터 타입이 일치하지 않습니다."),
  METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
  DATA_CONFLICT("데이터 무결성 제약 조건을 위반했습니다. (중복 데이터 등)"),
  FILE_SIZE_EXCEEDED("업로드 가능한 파일 크기를 초과했습니다."),
  INTERNAL_SERVER_ERROR("서버 내부 시스템 오류가 발생했습니다.");

  private final String message;

}
