package team.rescue.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 관련 에러
 * <p> code: 프론트 규약 에러 코드(HTTP Status Code로 구분 불가한 경우)
 */
@Getter
@RequiredArgsConstructor
public enum ServiceError {

	// File
	FILE_NOT_EXIST(HttpStatus.BAD_REQUEST, "파일을 등록해주세요."),
	FILE_EXTENSION_INVALID(HttpStatus.BAD_REQUEST, "유효한 파일이 아닙니다."),
	FILE_RESIZING_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 리사이징에 실패했습니다."),
	FILE_UPLOAD_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	FILE_PATH_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 타당하지 않습니다."),
	FILE_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제를 실패했습니다."),

	// User
	USER_ALREADY_LEAVE(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다."),
	EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
	EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	EMAIL_CODE_MIS_MATCH(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 일치하지 않습니다."),
	USER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호를 확인해주세요."),

	// Recipe
	RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 레시피를 찾을 수 없습니다."),
	RECIPE_MEMBER_UNMATCHED(HttpStatus.FORBIDDEN, "레시피를 작성한 회원이 아닙니다."),
	RECIPE_INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피 재료를 찾을 수 없습니다."),
	RECIPE_STEP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 레시피 스탭을 찾을 수 없습니다."),

	// Report
	REPORT_ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 신고한 레시입니다."),

	// Fridge
	FRIDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "냉장고 정보가 없습니다."),

	// FridgeIngredient
	INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료 정보가 없습니다."),

	// Notification
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 정보가 없습니다."),

	// Review
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보가 없습니다."),
	REVIEW_MEMBER_UNMATCHED(HttpStatus.FORBIDDEN, "리뷰를 작성한 회원이 아닙니다"),

	// search
	SEARCH_KEYWORD_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 키워드의 결과가 없습니다."),

	// Lock
	LOCK_ACQUISITION_FAIL(HttpStatus.BAD_REQUEST, "잠시 후 다시 시도해주세요."),
	LOCK_ALREADY_ASSIGNED(HttpStatus.BAD_REQUEST, "잠시 후 다시 시도해주세요.");


	private final HttpStatus httpStatus;
	private final String errorMessage;
}
