package team.rescue.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ImageUtil {

	private static final String FILE_SEPARATOR = ".";
	private static final String NAME_SEPARATOR = "_";

	/**
	 * 이미지 파일 업로드 이름 생성
	 *
	 * @param originName 원본 파일 이름
	 * @return 업로드 이름
	 */
	public String generateImageName(String originName) {

		String extension = getFileExtension(originName);
		String now = String.valueOf(System.currentTimeMillis()); // 파일 업로드 시간

		return now
				+ NAME_SEPARATOR
				+ RandomCodeUtil.generateUUID()
				+ FILE_SEPARATOR
				+ extension;
	}

	/**
	 * File 확장자 추출
	 *
	 * @param originName 원본 파일 이름
	 * @return 확장자
	 */
	private String getFileExtension(String originName) {

		int separatorPos = originName.lastIndexOf(FILE_SEPARATOR); // 구분
		return originName.substring(separatorPos); // 파일 확장자
	}
}
