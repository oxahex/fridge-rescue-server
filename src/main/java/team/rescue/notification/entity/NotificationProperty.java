package team.rescue.notification.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationProperty {

	private Long originId;            // 알림 발생 주체 ID (유통기한 지난 재료 ID, 후기 달린 레시피 ID, 추천 레시피 ID)
	private Long originUserId;        // 알림 발생 유저 ID(유통기한 지난 재료 = null, 레시피 후기 남긴 유저 ID, 오늘의 추천 레시피 = null)
	private String contents;          // 유통기한 지난 재료 이름, 후기 제목, 추천 레시피 제목

	@Builder
	public NotificationProperty(
			Long originId,
			Long originUserId,
			String contents
	) {

		this.originId = originId;
		this.originUserId = originUserId;
		this.contents = contents;
	}
}
