package team.rescue.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 리뷰 관련 에러
 * <p> code: 프론트 규약 에러 코드(HTTP Status Code로 구분 불가한 경우)
 */
@Getter
@RequiredArgsConstructor
public enum ReviewError {

	NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "이미 삭제된 리뷰입니다.");

	private final HttpStatus httpStatus;
	private final String errorMessage;
}
