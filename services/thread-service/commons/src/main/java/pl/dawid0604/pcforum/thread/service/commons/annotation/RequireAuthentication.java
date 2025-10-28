package pl.dawid0604.pcforum.thread.service.commons.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require authenticated user state.
 *     When condition is not met then
 *     {@link org.springframework.security.authorization.AuthorizationDeniedException}
 *     exception is thrown.
 * </p>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@PreAuthorize("isAuthenticated()")
public @interface RequireAuthentication { }
