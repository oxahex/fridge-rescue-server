package team.rescue.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.rescue.member.entity.Member;
import team.rescue.notification.entity.Notification;
import team.rescue.notification.entity.NotificationProperty;
import team.rescue.notification.type.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByMember(Member member, Pageable pageable);

	boolean existsByMemberAndNotificationTypeAndNotificationProperty(Member member,
			NotificationType notificationType, NotificationProperty notificationProperty);

}
