package team.rescue.search.entity;

import jakarta.persistence.Id;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import team.rescue.auth.type.RoleType;
import team.rescue.member.entity.Member;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.entity.RecipeIngredient;

@Document(indexName = "recipes")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeDoc {

	@Id
	private Long id;

	@Field(type = FieldType.Text)
	private String title;

	@Field(type = FieldType.Text)
	private String summary;

	@Field(type = FieldType.Integer)
	private Integer viewCount;

	@Field(type = FieldType.Integer)
	private Integer reviewCount;

	@Field(type = FieldType.Text)
	private String ingredients;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private ZonedDateTime createdAt;

	@Field(type = FieldType.Long)
	private Long memberId;

	@Field(type = FieldType.Text)
	private String memberNickname;

	@Field(type = FieldType.Text)
	private RoleType memberRole;

	@Field(type = FieldType.Text)
	private String image;

	public static RecipeDoc of(
			Recipe recipe, List<RecipeIngredient> ingredients, Member member, String recipeImageFilePath) {
		return RecipeDoc.builder()
				.id(recipe.getId())
				.title(recipe.getTitle())
				.summary(recipe.getSummary())
				.viewCount(recipe.getViewCount())
				.reviewCount(recipe.getReviewCount())
				.ingredients(ingredients.stream()
						.map(RecipeIngredient::getName)
						.collect(Collectors.joining(" ")))
				.createdAt(recipe.getCreatedAt().atZone(ZoneId.systemDefault()))
				.memberId(member.getId())
				.memberNickname(member.getNickname())
				.memberRole(member.getRole())
				.image(recipeImageFilePath)
				.build();
	}
}
