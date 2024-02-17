package team.rescue.recipe.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class BookmarkDto {

	@Getter
	@Setter
	@Builder
	public static class BookmarkInfoDto {

		private Integer bookmarkCount;
		private Boolean isBookmarked;
	}

}
