package pl.dawid0604.pcforum.thread.service.commons.annotation;

import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Order(1)
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@PreAuthorize("isAuthenticated() and hasAnyRole('MODERATOR', 'ADMIN')")
public @interface RequireModeratorAdminRole { }
