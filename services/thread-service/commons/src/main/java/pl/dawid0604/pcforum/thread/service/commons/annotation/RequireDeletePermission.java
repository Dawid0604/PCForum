package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require {@code DELETE} permissions
 *     for given resource.
 * </p>
 *
 * @see RequirePermission
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@RequirePermission(RequirePermission.Permission.DELETE)
public @interface RequireDeletePermission { }
