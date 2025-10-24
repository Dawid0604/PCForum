package pl.dawid0604.pcforum.thread.service.commons.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ PARAMETER, FIELD })
@Constraint(validatedBy = ValidContentValidator.class)
public @interface ValidContent {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String fieldName();
}
