package team.rescue.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe_step")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeStep {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_step_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipe_id")
	private Recipe recipe;

	@Column(name = "step_no")
	private int stepNo;

	@Column(name = "step_image_url", length = 150)
	private String stepImageUrl;

	@Column(name = "step_description", nullable = false, length = 200)
	private String stepDescription;

	@Column(name = "step_tip", nullable = false, length = 200)
	private String stepTip;

	public void updateRecipeStep(int stepNo, String stepImageUrl, String stepContents,
			String stepTip) {
		this.stepNo = stepNo;
		this.stepImageUrl = stepImageUrl;
		this.stepDescription = stepContents;
		this.stepTip = stepTip;
	}

	public void updateRecipeStepWithoutImage(int stepNo, String stepContents, String stepTip) {
		this.stepNo = stepNo;
		this.stepDescription = stepContents;
		this.stepTip = stepTip;
	}
}
