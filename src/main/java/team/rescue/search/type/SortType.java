package team.rescue.search.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Getter
@RequiredArgsConstructor
public enum SortType {
	HOT("reviewCount", Direction.ASC), DESC("createdAt", Direction.DESC);

	private final String sortBy;
	private final Sort.Direction direction;
}
