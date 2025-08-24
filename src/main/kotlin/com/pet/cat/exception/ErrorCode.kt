package com.pet.cat.exception

enum class ErrorCode(val status: Int, val code: String, val message: String) {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "입력값이 유효하지 않습니다."),
    METHOD_NOT_ALLOWED(405, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(404, "C003", "요청한 엔티티를 찾을 수 없습니다."),
    MISSING_PARAMETER(400, "C004", "필수값이 존재하지 않습니다."),
    INVALID_TYPE_VALUE(400, "C005", "잘못된 타입의 값이 입력되었습니다."),
    HANDLE_ACCESS_DENIED(403, "C006", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(500, "C007", "알 수 없는 에러가 발생하였습니다."),

    // Token
    ACCESS_TOKEN_EXPIRED(403, "T001", "Access token is expired"),
    REFRESH_TOKEN_NOT_COLLECT_DB(403, "T002", "refresh is different with db"),

    REFRESH_TOKEN_NOT_FOUND(403, "T003", "refresh token not found in cookie"),

    REFRESH_TOKEN_NOT_VALID(403, "T004", "refresh token invalid"),

    REFRESH_TOKEN_EXPIRED(403, "T005", "refresh token is expired"),

    // Goal
    DUPLICATE_NAME_GOAL_CREATE(400, "G001", "목표 이름이 중복됩니다."),
    GOAL_CREATE_FAIL(400, "G002", "목표 생성에 실패하였습니다."),
    GOAL_UPDATE_FAIL(400, "G003", "목표 수정에 실패하였습니다."),
    GOAL_DELETE_FAIL(400, "G004", "목표 삭제에 실패하였습니다."),
    GOAL_NOT_FOUND(400, "G005", "해당 목표가 존재하지 않습니다."),


    // Member
    EMAIL_DUPLICATION(400, "M001", "이미 존재하는 이메일입니다."),
    LOGIN_INPUT_INVALID(400, "M002", "Login input is invalid"),

    MEMBER_NOT_FOUND(400, "M003", "존재하지 않는 아이디입니다."),

    MEMBER_NOT_ALLOWED(400, "M003", "유효하지 않은 아이디입니다."),

    EXIST_USER_ID(400, "M004", "이미 존재하는 아이디입니다."),

    EXIST_USER_NICK_NAME(400, "M005", "입력하신 닉네임이 이미 존재합니다."),

    EXIST_USER_EMAIL(400, "M006", "입력하신 이메일이 이미 존재합니다."),

    REDIS_MEMBER_DEL_ERROR(400, "M007", "회원가입 과정에서 에러가 발생하였습니다."),

    INVALID_ID_OR_PASSWORD(400, "M008", "아이디 혹은 비밀번호를 잘못 입력하셨습니다."),

    EXCEEED_SEND_EMAIL_LIMIT_10(400, "M009", "메일 인증은 1일 10회까지만 가능합니다."),

    PRESENT_PASSWORD_INCORRECT(400, "M010", "현재 비밀번호가 일치하지 않습니다."),

    PRESENT_PASSWORD_SAME_WITH_NEW(400, "M011", "새로운 비밀번호가 기존 비밀번호와 동일합니다."),

    IS_SAME_PRE_NICKNAME(400, "M012", "기존과 닉네임이 동일합니다."),

    INVALID_EMAIL(400, "M013", "유효하지 않은 이메일입니다."),

    INVALID_CERTIF_NUMBER(400, "M014", "인증번호가 유효하지 않습니다."),


    USER_IS_DELETED(400, "M015", "탈퇴된 아이디입니다."),

    SIGNIN_LOCKED_3M(400, "M016", "최대 로그인 시도 가능 횟수를 초과하였습니다. 3분후 재시도해주세요."),
    SIGNIN_LOCKED_10M(400, "M017", "최대 로그인 시도 가능 횟수를 초과하였습니다. 10분후 재시도해주세요."),
    SIGNIN_LOCKED_60M(400, "M018", "최대 로그인 시도 가능 횟수를 초과하였습니다. 1시간 후 재시도해주세요."),

    // Board
    EXCEED_CREATE_POST_LIMIT_1_MIN(400, "B001", "글을 너무 자주 작성하실 수 없습니다."),

    EXCEED_CREATE_REVIEW_LIMIT_1_MIN(400, "B002", "리뷰를 너무 자주 작성하실 수 없습니다."),

    DELETED_BOARD(400, "B003", "삭제된 게시글입니다."),

    INVALID_AUTHCODE(400, "AU001", "유효하지 않은 인증정보입니다."),

    // Image
    IMAGE_UPLOAD_FAIL(500, "IM001", "이미지 업로드 실패"),

}