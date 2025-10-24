package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
@RequirePermission(RequirePermission.Permission.EDIT)
public @interface RequireEditPermission {
    int interval();
}
