package team.rescue.fridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fridge_ingredient")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeIngredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "fridge_ingredient_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fridge_id", nullable = false)
	private Fridge fridge;

	@Column(name = "name", nullable = false, length = 20)
	private String name;

	@Column(name = "memo", nullable = false, length = 20)
	private String memo;

	@Column(name = "expired_at")
	private LocalDate expiredAt;

	public void updateFridgeIngredient(String name, String memo, LocalDate expiredAt) {
		this.name = name;
		this.memo = memo;
		this.expiredAt = expiredAt;
	}
}
