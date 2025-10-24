package pl.dawid0604.pcforum.thread.service.commons.annotation;

import com.modernmt.text.profanity.ProfanityFilter;
import com.modernmt.text.profanity.dictionary.Profanity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

class ValidContentValidator implements ConstraintValidator<ValidContent, Object> {
    private static final ProfanityFilter PROFANITY_FILTER = new ProfanityFilter();
    private static final String DEFAULT_MESSAGE = "Field '%s' should not contains profanities and spam. Violation word: '%s'";
    private String fieldName;

    @Override
    public void initialize(final ValidContent constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if(value instanceof String stringValue && isNotBlank(stringValue)) {
            final Profanity profanity = PROFANITY_FILTER.find(Locale.ENGLISH.getLanguage(), stringValue);

            if(profanity != null) {
                context.buildConstraintViolationWithTemplate(getViolationMessage(profanity.text()))
                       .addConstraintViolation();

                return false;
            }
        }

        return true;
    }

    private String getViolationMessage(final String profanityWord) {
        return DEFAULT_MESSAGE.formatted(fieldName, profanityWord);
    }
}
