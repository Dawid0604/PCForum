package pl.dawid0604.pcforum.category.service.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import pl.dawid0604.pcforum.category.service.commons.exception.RateLimitException;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestExceptionHandler Tests")
class RestExceptionHandlerTest {
    private static final String SERVICE_NAME = "Category Service";
    private static final Faker FAKER = new Faker();

    @SuppressWarnings("unused")
    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void setUp() {
        this.restExceptionHandler = new RestExceptionHandler(SERVICE_NAME);
    }

    @Test
    @DisplayName("Should handle UnexpectedException")
    void shouldShouldHandleUnexpectedException() {
        // Given
        final IllegalArgumentException exception = new IllegalArgumentException(
                FAKER.lorem().sentence()
        );

        // When
        final ProblemDetail result = restExceptionHandler.handleUnexpectedException(exception);

        // Then
        Assertions.assertThat(result)
                  .isNotNull();

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "ISE_URI"));

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "Generic");

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }

    @Test
    @DisplayName("Should handle NoSuchElementException")
    void shouldShouldHandleNoSuchElementException() {
        // Given
        final NoSuchElementException exception = new NoSuchElementException(
                FAKER.lorem().sentence()
        );

        // When
        final ProblemDetail result = restExceptionHandler.handleNotFoundResourceException(exception);

        // Then
        Assertions.assertThat(result)
                  .isNotNull();

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.NOT_FOUND.value());

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "RESOURCE_NOT_FOUND_URI"));

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "Resource");

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }

    @Test
    @DisplayName("Should handle AuthorizationDeniedException")
    void shouldShouldHandleAuthorizationDeniedException() {
        // Given
        final AuthorizationDeniedException exception = new AuthorizationDeniedException(
                FAKER.lorem().sentence()
        );

        // When
        final ProblemDetail result = restExceptionHandler.handleUnauthorized(exception);

        // Then
        Assertions.assertThat(result)
                  .isNotNull();

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.FORBIDDEN.value());

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "UNAUTHORIZED_URI"));

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "No Permissions");

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void shouldShouldHandleConstraintViolationException() {
        // Given
        ConstraintViolation<?> firstViolation = mock(ConstraintViolation.class);
        ConstraintViolation<?> secondViolation = mock(ConstraintViolation.class);
        ConstraintViolation<?> thirdViolation = mock(ConstraintViolation.class);

        final String firstViolationMessage = "Name cannot be empty";
        final String secondViolationMessage = "CategoryId should be 21 characters";
        final String thirdViolationMessage = "Unknown field";

        final String firstPropertyPath = "name";
        final String secondPropertyPath = "categoryId";
        final String thirdPropertyPath = "";

        final String firstInvalidValue = "";
        final String secondInvalidValue = FAKER.lorem().characters(1, 19);
        final String thirdInvalidValue = FAKER.lorem().characters(1, 19);

        final Path firstViolationPath = createMockPath(firstPropertyPath);
        final Path secondViolationPath = createMockPath(secondPropertyPath);
        final Path thirdViolationPath = createMockPath(thirdPropertyPath);

        when(firstViolation.getMessage())
                .thenReturn(firstViolationMessage);

        when(secondViolation.getMessage())
                .thenReturn(secondViolationMessage);

        when(thirdViolation.getMessage())
                .thenReturn(thirdViolationMessage);

        when(firstViolation.getPropertyPath())
                .thenReturn(firstViolationPath);

        when(secondViolation.getPropertyPath())
                .thenReturn(secondViolationPath);

        when(thirdViolation.getPropertyPath())
                .thenReturn(thirdViolationPath);

        when(firstViolation.getInvalidValue())
                .thenReturn(firstInvalidValue);

        when(secondViolation.getInvalidValue())
                .thenReturn(secondInvalidValue);

        when(thirdViolation.getInvalidValue())
                .thenReturn(thirdInvalidValue);

        final Set<ConstraintViolation<?>> constraintViolations = Set.of(
                firstViolation, secondViolation, thirdViolation
        );

        final ConstraintViolationException exception = new ConstraintViolationException(
                constraintViolations
        );

        // When
        final ProblemDetail result = restExceptionHandler.handleConstraintViolationException(exception);

        // Then
        Assertions.assertThat(result)
                  .isNotNull();

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "VALIDATION_URI"));

        Assertions.assertThat(result)
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "Validation");

                      Assertions.assertThat(p)
                                .extracting(tp -> tp.get("violations"))
                                .asInstanceOf(InstanceOfAssertFactories.LIST)
                                .containsExactlyInAnyOrder(
                                        Map.of(
                                                "rejectedValue", firstInvalidValue,
                                                "field", firstPropertyPath,
                                                "message", firstViolationMessage
                                        ),

                                        Map.of(
                                                "rejectedValue", secondInvalidValue,
                                                "field", secondPropertyPath,
                                                "message", secondViolationMessage
                                        ),

                                        Map.of(
                                                "rejectedValue", thirdInvalidValue,
                                                "field", "unknown",
                                                "message", thirdViolationMessage
                                        )
                                );

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }

    private Path createMockPath(final String fieldName) {
        final Path path = mock(Path.class);
        final Path.Node node = mock(Path.Node.class);
        final List<Path.Node> nodes = List.of(node);

        when(node.getName())
                .thenReturn(fieldName);

        when(path.iterator())
                .thenReturn(nodes.iterator());

        return path;
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldShouldHandleMethodArgumentNotValidException() {
        // Given
        final String firstViolationMessage = "Name cannot be empty";
        final String secondViolationMessage = "CategoryId should be 21 characters";
        final String firstFieldName = "name";
        final String secondFieldName = "categoryId";
        final String thirdFieldName = "lastName";

        final MethodParameter methodParameter = mock(MethodParameter.class);
        final BindingResult bindingResult = mock(BindingResult.class);
        final Method method = mock(Method.class);

        final FieldError firstFieldError = new FieldError(
                "anyDto",
                firstFieldName,
                firstViolationMessage
        );

        final FieldError secondFieldError = new FieldError(
                "anyDto",
                secondFieldName,
                secondViolationMessage
        );

        final FieldError thirdFieldError = new FieldError(
                "anyDto",
                thirdFieldName,
                ""
        );

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(firstFieldError, secondFieldError, thirdFieldError));

        when(bindingResult.getAllErrors())
                .thenReturn(List.of(firstFieldError, secondFieldError, thirdFieldError));

        when(methodParameter.getExecutable())
                .thenReturn(method);

        when(method.toGenericString())
                .thenReturn("anyMethod");

        final MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        final HttpHeaders headers = new HttpHeaders();
        final HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(400);
        final WebRequest request = mock(WebRequest.class);

        // When
        final ResponseEntity<Object> result = restExceptionHandler.handleMethodArgumentNotValid(exception, headers, httpStatusCode, request);

        // Then
        Assertions.assertThat(result)
                .isNotNull();

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .asInstanceOf(InstanceOfAssertFactories.type(ProblemDetail.class))
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .asInstanceOf(InstanceOfAssertFactories.type(ProblemDetail.class))
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .asInstanceOf(InstanceOfAssertFactories.type(ProblemDetail.class))
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .asInstanceOf(InstanceOfAssertFactories.type(ProblemDetail.class))
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "VALIDATION_URI"));

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .asInstanceOf(InstanceOfAssertFactories.type(ProblemDetail.class))
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "Validation");

                      Assertions.assertThat(p)
                                .extracting(tp -> tp.get("violations"))
                                .asInstanceOf(InstanceOfAssertFactories.LIST)
                                .containsExactlyInAnyOrder(
                                        Map.of(
                                                "field", firstFieldName,
                                                "message", firstViolationMessage
                                        ),

                                        Map.of(
                                                "field", secondFieldName,
                                                "message", secondViolationMessage
                                        ),

                                        Map.of(
                                                "field", thirdFieldName,
                                                "message", "No data"
                                        )
                                );

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }

    @Test
    @DisplayName("Should handle RateLimitException")
    void shouldShouldHandleRateLimitException() {
        // Given
        final Pair<String, String> limitHeader = Pair.of(
                "X-RateLimit-Limit", "60"
        );

        final Pair<String, String> limitRemainingHeader = Pair.of(
                "X-RateLimit-Remaining", "59"
        );

        final Pair<String, String> limitResetHeader = Pair.of(
                "X-RateLimit-Reset", String.valueOf(Timestamp.from(Instant.now()).getTime())
        );

        final Pair<String, String> retryAfterHeader = Pair.of(
                "Retry-After", String.valueOf(Timestamp.from(Instant.now().plusSeconds(60)).getTime())
        );

        final String remoteAddr = FAKER.internet().publicIpV4Address();
        final String requestUri = "/api/v1/category";
        final String requestMethod = "GET";
        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn(remoteAddr);

        when(request.getRequestURI())
                .thenReturn(requestUri);

        when(request.getMethod())
                .thenReturn(requestMethod);

        final RateLimitException exception = new RateLimitException(
                limitHeader,
                limitRemainingHeader,
                limitResetHeader,
                retryAfterHeader
        );

        // When
        final ResponseEntity<ProblemDetail> result = restExceptionHandler.handleRateLimitException(exception, request);

        // Then
        Assertions.assertThat(result)
                  .isNotNull()
                  .extracting(ResponseEntity::getStatusCode)
                  .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .extracting(ProblemDetail::getStatus)
                  .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .extracting(ProblemDetail::getTitle)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .extracting(ProblemDetail::getDetail)
                  .matches(StringUtils::isNotBlank);

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .extracting(ProblemDetail::getType)
                  .isEqualTo(ReflectionTestUtils.getField(restExceptionHandler, "RATE_LIMIT_URI"));

        Assertions.assertThat(result)
                  .extracting(ResponseEntity::getBody)
                  .extracting(ProblemDetail::getProperties)
                  .satisfies(p -> {
                      Assertions.assertThat(p)
                                .containsEntry("service", SERVICE_NAME);

                      Assertions.assertThat(p)
                                .containsEntry("error_category", "To many requests");

                      Assertions.assertThat(p)
                                .containsEntry("path", requestUri);

                      Assertions.assertThat(p)
                                .containsEntry("method", requestMethod);

                      Assertions.assertThat(p)
                                .containsEntry("clientIp", remoteAddr);

                      Assertions.assertThat(p.get("timestamp"))
                                .isInstanceOf(Instant.class)
                                .isNotNull();
                  });
    }
}