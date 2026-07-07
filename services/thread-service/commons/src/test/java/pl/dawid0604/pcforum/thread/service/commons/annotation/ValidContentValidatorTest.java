package pl.dawid0604.pcforum.thread.service.commons.annotation;

import jakarta.validation.ConstraintValidatorContext;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidContentValidator tests")
class ValidContentValidatorTest {

    private static final Faker FAKER = new Faker();
    private ValidContentValidator validContentValidator;

    @Mock
    private ValidContent annotation;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validContentValidator = new ValidContentValidator();
    }

    @Nested
    @DisplayName("initialize() tests")
    class InitializeTests {

        @ParameterizedTest
        @MethodSource("shouldSetFieldNameDataProvider")
        void shouldSetFieldName(final String fieldName) {
            // Given
            when(annotation.fieldName())
                    .thenReturn(fieldName);

            // When
            validContentValidator.initialize(annotation);

            // Then
            verify(annotation).fieldName();
        }

        private static Stream<Arguments> shouldSetFieldNameDataProvider() {
            return Stream.of(
                    Arguments.of(FAKER.word().noun()),
                    Arguments.of(StringUtils.EMPTY),
                    Arguments.of((Object) null)
            );
        }
    }

    @Nested
    @DisplayName("isValid() tests")
    class IsValidTests {

        @Captor
        private ArgumentCaptor<String> validationMessageArgumentCaptor;

        @RepeatedTest(20)
        void shouldReturnTrueForValidInput() {
            // Given
            final String value = FAKER.options()
                                      .option(
                                              FAKER.word().noun(),
                                              FAKER.word().adjective(),
                                              FAKER.lorem().sentence(),
                                              null,
                                              StringUtils.SPACE,
                                              StringUtils.EMPTY
                                      );

            // When
            final boolean result = validContentValidator.isValid(value, context);

            // Then
            Assertions.assertThat(result)
                      .isTrue();

            verifyNoInteractions(context);
        }

        @Test
        void shouldReturnFalseForInvalidInput() {
            // Given
            final String value = "shit";
            final ConstraintValidatorContext.ConstraintViolationBuilder builder =
                    mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(builder);

            when(builder.addConstraintViolation())
                    .thenReturn(context);

            // When
            final boolean result = validContentValidator.isValid(value, context);

            // Then
            verify(context).buildConstraintViolationWithTemplate(validationMessageArgumentCaptor.capture());

            Assertions.assertThat(result)
                      .isFalse();

            Assertions.assertThat(validationMessageArgumentCaptor.getValue())
                      .isNotNull()
                      .isNotBlank();
        }
    }
}