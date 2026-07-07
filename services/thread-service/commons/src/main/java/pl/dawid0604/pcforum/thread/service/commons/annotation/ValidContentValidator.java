package pl.dawid0604.pcforum.thread.service.commons.annotation;

import com.modernmt.text.profanity.ProfanityFilter;
import com.modernmt.text.profanity.dictionary.Profanity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;

import java.util.Locale;

import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p>
 *     The {@link ValidContent} annotation constraint implementation.
 * </p>
 */
@NoArgsConstructor(access = PACKAGE)
final class ValidContentValidator implements ConstraintValidator<ValidContent, Object> {

    /**
     * <p>
     *     Filter to detect profanities for given text input.
     * </p>
     */
    private static final ProfanityFilter PROFANITY_FILTER;

    /**
     * <p>
     *     Default failure validation message.
     * </p>
     */
    private static final String DEFAULT_MESSAGE;

    /**
     * <p>
     *     Current validated field.
     * </p>
     */
    private String fieldName;

    static {
        PROFANITY_FILTER = new ProfanityFilter();
        DEFAULT_MESSAGE = "Field '%s' should not contains profanities and spam. Violation word: '%s'";
    }

    @Override
    public void initialize(final ValidContent constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        boolean isValid = true;

        if (value instanceof String stringValue && isNotBlank(stringValue)) {
            final Profanity profanity = PROFANITY_FILTER.find(Locale.ENGLISH.getLanguage(), stringValue);

            if (profanity != null) {
                context.buildConstraintViolationWithTemplate(getViolationMessage(profanity.text()))
                       .addConstraintViolation();

                isValid = false;
            }
        }

        return isValid;
    }

    private String getViolationMessage(final String profanityWord) {
        return DEFAULT_MESSAGE.formatted(fieldName, profanityWord);
    }
}
