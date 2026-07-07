package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require {@code EDIT} permissions
 *     for given resource.
 * </p>
 *
 * @see RequirePermission
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@RequirePermission(RequirePermission.Permission.EDIT)
public @interface RequireEditPermission {

    /**
     * <p>
     *     Value to determine interval between edits
     *     determined by the unit of minutes.
     * </p>
     * @return interval between resource edits
     */

    int interval();
}
