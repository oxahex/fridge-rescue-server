package team.rescue.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.rescue.auth.type.ProviderType;
import team.rescue.auth.type.RoleType;
import team.rescue.cook.entity.Cook;
import team.rescue.fridge.entity.Fridge;
import team.rescue.notification.entity.Notification;
import team.rescue.review.entity.Review;

@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(name = "nickname", nullable = false, length = 15)
	private String nickname;

	@Column(name = "email", unique = true, nullable = false, length = 50)
	private String email;

	@Column(name = "password", nullable = false, length = 100)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 10)
	private RoleType role;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 10)
	private ProviderType provider;

	@Column(name = "provider_id", length = 100)
	private String providerId;

	@Column(name = "jwt_token")
	private String token;

	@Column(name = "is_enabled", nullable = false)
	private Boolean isEnabled;

	@OneToOne(mappedBy = "member")
	private Fridge fridge;

	// 알림 조회
	@Builder.Default
	@OneToMany(mappedBy = "member")
	private List<Notification> notificationList = new ArrayList<>();

	// 요리 완료 조회
	@Builder.Default
	@OneToMany(mappedBy = "member")
	private List<Cook> cookList = new ArrayList<>();

	// 레시피 후기 조회
	@Builder.Default
	@OneToMany(mappedBy = "member")
	private List<Review> reviewList = new ArrayList<>();

	@CreatedDate
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "modified_at")
	private LocalDateTime modifiedAt;

	public void updateRole(RoleType role) {
		this.role = role;
	}

	public void registerFridge(Fridge fridge) {
		this.fridge = fridge;
	}

	public void updateToken(String token) {
		this.token = token;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	// 회원 탈퇴
	public void leave() {
		this.isEnabled = false;
	}
}

