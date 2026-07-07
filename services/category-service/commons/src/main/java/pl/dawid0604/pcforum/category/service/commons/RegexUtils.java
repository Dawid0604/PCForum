package pl.dawid0604.pcforum.category.service.commons;

import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;


/**
 * <p>
 *    Utility regex class.
 * </p>
 */
public final class RegexUtils {

    /**
     * Widely used comma pattern.
     */
    public static final Pattern COMMA_PATTERN = patternFrom(",");

    /**
     * <p>
     *     Method to create pattern for the given regex.
     * </p>
     *
     * <p>
     *     <strong>Pattern is compiled with the following flags:</strong>
     *     <ul>
     *         <li>
     *             {@link Pattern#CASE_INSENSITIVE}
     *         </li>
     *
     *         <li>
     *             {@link Pattern#CANON_EQ}
     *         </li>
     *
     *         <li>
     *             {@link Pattern#UNICODE_CASE}
     *         </li>
     *
     *         <li>
     *             {@link Pattern#UNICODE_CHARACTER_CLASS}
     *         </li>
     *     </ul>
     * </p>
     * @param regex cannot be null or blank.
     * @throws IllegalArgumentException when regex is null or blank.
     * @return compiled {@link Pattern} object.
     */
    public static Pattern patternFrom(final String regex) {
        return Optional.ofNullable(regex)
                       .filter(not(String::isBlank))
                       .map(RegexUtils::compile)
                       .orElseThrow(() -> new IllegalArgumentException("Regex cannot be null or blank"));
    }

    /**
     * <p>
     *     Auxiliary method to compile regex.
     * </p>
     *
     * @param regex cannot be null or blank
     * @return compiled {@link Pattern} with {@code regex}
     */
    private static Pattern compile(
            @NonNull
            final String regex) {

        return Pattern.compile(
                regex,
                Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS
        );
    }

    /**
     * <p>
     *     Method to split text by given pattern
     *     and extract desired split value by index.
     * </p>
     * @param pattern compiled {@link Pattern} object. It cannot be null.
     * @param input cannot be null or blank.
     * @param index must be greater or equal than 0.
     * @throws IllegalArgumentException when given parameters are invalid.
     * @throws ArrayIndexOutOfBoundsException when value under {@code index} is not present.
     * @return split value by desired index.
     */
    public static String split(final Pattern pattern, final String input, final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }

        if (pattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }

        return Optional.ofNullable(input)
                       .filter(not(String::isBlank))
                       .map(i -> pattern.split(i)[index])
                       .map(String::trim)
                       .orElseThrow(() -> new IllegalArgumentException(
                               "Input cannot be split:: Input: %s, Pattern: %s".formatted(input, pattern.pattern()))
                       );
    }

    /**
     * Creating class instance is prohibited.
     */
    private RegexUtils() { }
}
