package team.rescue.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseDto<T> {

	private String message = null;
	private final T data;

	@Builder
	public ResponseDto(String message, T data) {
		this.message = message;
		this.data = data;
	}
}
