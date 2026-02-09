package tribefire.extension.auth.rbac.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface DenyRoles {
	String globalId() default "";
	/** A set of role names for which access/execution/etc is denied */
	String[] value();
}
