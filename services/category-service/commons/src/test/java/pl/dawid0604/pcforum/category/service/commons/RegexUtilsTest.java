package pl.dawid0604.pcforum.category.service.commons;

import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@DisplayName("RegexUtils tests")
class RegexUtilsTest {
    private static final Faker FAKER = new Faker();

    @Nested
    @DisplayName("Pattern creation tests")
    class PatternCreationTests {

        @RepeatedTest(10)
        @DisplayName("Should create pattern for random valid regexes")
        void shouldCreatePatternForRandomValidRegexes() {
            // Given
            final List<String> validRegexes = List.of(
                    FAKER.regexify("[a-z]{3,10}"),
                    FAKER.regexify("[a-z]{1,2}-[0-9]{3,10}"),
                    "\\w+@\\w+",
                    "\\b\\w{" + FAKER.number().numberBetween(5, 55) + "}\\b"
            );

            final String randomRegex = FAKER.options()
                                            .option(validRegexes.toArray(String[]::new));

            // When
            final Pattern result = RegexUtils.patternFrom(randomRegex);

            // Then
            Assertions.assertThat(result)
                      .isNotNull();

            Assertions.assertThat(result.pattern())
                      .isEqualTo(randomRegex);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "", " ", "\t", "\n", "\r\n"
        })
        @DisplayName("Should throw exception for invalid regex")
        void shouldThrowExceptionForInvalidRegex(final String invalidRegex) {
            // Given
            // When
            // Then
            Assertions.assertThatThrownBy(() -> RegexUtils.patternFrom(invalidRegex))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Regex cannot be null or blank");
        }

        @RepeatedTest(20)
        @DisplayName("Should handle case insensitive matching correctly")
        void shouldHandleCaseInsensitiveMatchingCorrectly() {
            // Given
            final List<String> randomWords = List.of(
                    FAKER.name().firstName(),
                    FAKER.name().fullName(),
                    FAKER.company().name(),
                    FAKER.company().buzzword(),
                    FAKER.color().name(),
                    FAKER.animal().name(),
                    FAKER.regexify("[A-Z][a-z]{1,2}[A-Z]")
            );

            final String randomWord = FAKER.options()
                                           .option(randomWords.toArray(String[]::new));

            // When
            final Pattern result = RegexUtils.patternFrom(".+");

            // Then
            Assertions.assertThat(result)
                      .isNotNull();

            Assertions.assertThat(result.matcher(randomWord)
                                        .matches())
                      .isTrue();
        }

        @RepeatedTest(20)
        @DisplayName("Should handle unicode matching correctly")
        void shouldHandleUnicodeMatchingCorrectly() {
            // Given
            final List<String> randomWords = List.of(
                    FAKER.regexify("[A-Za-ząćęłńóśźżĄĆĘŁŃÓŚŹŻ]{5}"),
                    FAKER.regexify("[IsGreek]{5}")
            );

            final String randomWord = FAKER.options()
                                           .option(randomWords.toArray(String[]::new));

            // When
            final Pattern result = RegexUtils.patternFrom("\\p{L}+");

            // Then
            Assertions.assertThat(result)
                      .isNotNull();

            Assertions.assertThat(result.matcher(randomWord)
                                        .matches())
                      .isTrue();
        }
    }

    @Nested
    @DisplayName("Split method tests")
    class SplitMethodTests {

        @RepeatedTest(20)
        @DisplayName("Should split comma-separated values")
        void shouldSplitCommaSeparatedValues() {
            // Given
            final List<String> randomTexts = Stream.generate(() -> FAKER.lorem().word())
                                                   .limit(FAKER.number().numberBetween(3, 10))
                                                   .toList();

            final String input = String.join(",", randomTexts);
            final int randomIndex = FAKER.number()
                                         .numberBetween(0, randomTexts.size());

            // When
            final String result = RegexUtils.split(RegexUtils.COMMA_PATTERN, input, randomIndex);

            // Then
            Assertions.assertThat(result)
                      .isNotNull()
                      .isEqualTo(randomTexts.get(randomIndex));
        }

        @RepeatedTest(20)
        @DisplayName("Should split with custom patterns")
        void shouldSplitWithCustomPatterns() {
            // Given
            final Map<String, String> separators = Map.of(
                    ";", ";",
                    "\\|", "|",
                    ":", ":",
                    "\\s+", FAKER.regexify("\\s{1,10}"),
                    "-", "-"
            );

            final String randomRegexSeparator = FAKER.options()
                                                     .option(separators.keySet().toArray(String[]::new));

            final Pattern pattern = RegexUtils.patternFrom(randomRegexSeparator);

            final List<String> randomTexts = Stream.generate(() -> FAKER.name().firstName())
                                                   .limit(FAKER.number().numberBetween(3, 10))
                                                   .toList();

            final String input = String.join(separators.get(randomRegexSeparator), randomTexts);

            final int randomIndex = FAKER.number()
                                         .numberBetween(0, randomTexts.size());

            // When
            final String result = RegexUtils.split(pattern, input, randomIndex);

            // Then
            Assertions.assertThat(result)
                      .isNotNull()
                      .isEqualTo(randomTexts.get(randomIndex));
        }

        @ParameterizedTest
        @ValueSource(ints = {
                -1, -5, -100
        })
        @DisplayName("Should throw exception for negative index")
        void shouldThrowExceptionForNegativeIndex(final int negativeIndex) {
            // Given
            final String input = "a, b, c";

            // When
            // Then
            Assertions.assertThatThrownBy(() -> RegexUtils.split(RegexUtils.COMMA_PATTERN, input, negativeIndex))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Index cannot be negative");
        }

        @ParameterizedTest
        @ValueSource(ints = {
                15, 25, 100
        })
        @DisplayName("Should throw exception for index out of bounds")
        void shouldThrowExceptionForIndexOutOfBounds(final int invalidIndex) {
            // Given
            final String input = "a, b, c";

            // When
            // Then
            Assertions.assertThatThrownBy(() -> RegexUtils.split(RegexUtils.COMMA_PATTERN, input, invalidIndex))
                      .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should throw exception for null pattern")
        void shouldThrowExceptionForNullPattern() {
            // Given
            final String input = "a, b, c";

            // When
            // Then
            Assertions.assertThatThrownBy(() -> RegexUtils.split(null, input, 0))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Pattern cannot be null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "", " ", "\t"
        })
        @DisplayName("Should throw exception for invalid input")
        void shouldThrowExceptionForInvalidInput(final String input) {
            // Given
            // When
            // Then
            Assertions.assertThatThrownBy(() -> RegexUtils.split(RegexUtils.COMMA_PATTERN, input, 0))
                      .isInstanceOf(IllegalArgumentException.class);
        }
    }
}