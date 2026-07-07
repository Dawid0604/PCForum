package pl.dawid0604.pcforum.thread.service.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Annotation to require proper permission
 *     determined by {@link Permission} enum constant.
 * </p>
 *
 * @see RequireDeletePermission
 * @see RequireEditPermission
 * @see RequireCreationPermission
 */
@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@interface RequirePermission {

    /**
     * <p>
     *     Field that indicates a required permission.
     * </p>
     * @return {@link Permission} constant.
     */
    Permission value();

    /**
     * <p>
     *     Enum that indicates type of permission.
     * </p>
     */
    enum Permission {

        /**
         * <p>
         *     Resource edits are allowed.
         * </p>
         */
        EDIT,

        /**
         * <p>
         *     Resource creations are allowed.
         * </p>
         */
        CREATE,

        /**
         * <p>
         *     Resource deletions are allowed.
         * </p>
         */
        DELETE
    }
}
