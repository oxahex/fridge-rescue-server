package team.rescue.common.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefix {

	TOKEN("TOKEN_", 1000 * 60 * 60 * 24L),
	CODE("CODE_", 1000 * 60 * 10L);

	private final String prefix;
	private final Long expiredTime;

}
