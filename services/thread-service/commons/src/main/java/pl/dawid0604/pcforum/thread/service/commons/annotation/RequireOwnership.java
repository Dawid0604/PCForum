package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface RequireOwnership {
    String message() default "No permissions to access this resource";

    String identifier();

    boolean allowAdmin() default true;

    boolean allowModerator() default true;
}