package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require creation permission with interval
 *     between creations. User can create resource only with
 *     strict interval determined by {@link #interval()} value.
 * </p>
 *
 * @see RequirePermission
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@RequirePermission(RequirePermission.Permission.CREATE)
public @interface RequireCreationPermission {

    /**
     * <p>
     *     Value to determine interval between creations
     *     determined by the unit of minutes.
     * </p>
     * @return interval between resource creations
     */
    int interval();
}
