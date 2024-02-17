package team.rescue.auth.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtTokenType {
	ACCESS_TOKEN(1000 * 60 * 60),               // 1 hour
	REFRESH_TOKEN(1000 * 60 * 60 * 24 * 7),     // 1 week
	TEST_TOKEN(1000);                           // 1 sec(for test)

	private final long expireTime;
}
