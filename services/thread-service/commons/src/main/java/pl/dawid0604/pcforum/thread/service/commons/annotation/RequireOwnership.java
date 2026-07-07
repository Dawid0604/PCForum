package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require ownership permission to use given
 *     resource. Default only <b>ownership user</b> has a permission
 *     and <b>Admin</b> with <b>Moderator</b>. The <b>Moderator</b>
 *     permission can be customized via {@link #allowModerator()} field.
 * </p>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface RequireOwnership {

    /**
     * @return annotation default message.
     */
    String message() default "No permissions to access this resource";

    /**
     * @return annotation identifier field.
     */
    String identifier();

    /**
     * @return {@code true} when moderator has permissions
     * and {@code false} otherwise.
     */
    boolean allowModerator() default true;
}
