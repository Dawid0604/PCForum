package pl.dawid0604.pcforum.thread.service.commons.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to validate given content.
 * </p>
 *
 * @see ValidContentValidator
 */
@Documented
@Retention(RUNTIME)
@Target({ PARAMETER, FIELD })
@Constraint(validatedBy = ValidContentValidator.class)
public @interface ValidContent {

    /**
     * @return annotation default message.
     */
    String message() default "";

    /**
     * @return annotation groups.
     */
    Class<?>[] groups() default { };

    /**
     * @return annotation payload.
     */
    Class<? extends Payload>[] payload() default { };

    /**
     * @return validated FieldName.
     */
    String fieldName();
}
