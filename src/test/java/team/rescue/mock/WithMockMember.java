package team.rescue.mock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;
import team.rescue.auth.type.RoleType;

/**
 * value = email, role = RoleType
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberSecurityFactory.class)
public @interface WithMockMember {

	String value() default "test@gmail.com";

	RoleType role() default RoleType.GUEST;
}
