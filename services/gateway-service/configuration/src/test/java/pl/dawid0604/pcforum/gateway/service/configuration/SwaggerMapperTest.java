package pl.dawid0604.pcforum.gateway.service.configuration;

import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.stream.Stream;

import static lombok.AccessLevel.PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@NoArgsConstructor(access = PACKAGE)
class SwaggerMapperTest {

    @Nested
    @DisplayName("Success")
    class Success {

        @ParameterizedTest
        @MethodSource("isValidDataSource")
        @DisplayName("isValid(): Should not throw any exception")
        void isValid(final RouteDefinition routeDefinition, final boolean expectedValue) {
            // Given
            // When
            // Then
            Assertions.assertThat(SwaggerMapper.isValid(routeDefinition))
                      .isEqualTo(expectedValue);
        }

        @ParameterizedTest
        @MethodSource("mapDataSource")
        @DisplayName("map(): Should not throw any exception")
        void map(final RouteDefinition routeDefinition, final SwaggerUrl expectedValue) {
            // Given
            // When
            // Then
            Assertions.assertThat(SwaggerMapper.map(routeDefinition))
                      .isEqualTo(expectedValue);
        }

        @ParameterizedTest
        @MethodSource("getApiDocsUrlDataSource")
        @DisplayName("getApiDocsUrl(): Should not throw any exception")
        void getApiDocsUrl(final String serviceId, final String expectedSuffix) {
            // Given
            // When
            // Then
            Assertions.assertThat(SwaggerMapper.getApiDocsUrl(serviceId))
                      .endsWith(expectedSuffix);
        }

        private static Stream<Arguments> getApiDocsUrlDataSource() {
            return Stream.of(
                    Arguments.of("my-service", "/my"),
                    Arguments.of("my-super-service",  "/my-super")
            );
        }

        private static Stream<Arguments> mapDataSource() {
            RouteDefinition firstRouteDefinition = new RouteDefinition();
                            firstRouteDefinition.setId("my-service");

            RouteDefinition secondRouteDefinition = new RouteDefinition();
                            secondRouteDefinition.setId("my-super-service");

            return Stream.of(
                    Arguments.of(firstRouteDefinition, new SwaggerUrl(firstRouteDefinition.getId(),
                            SwaggerMapper.getApiDocsUrl(firstRouteDefinition.getId()),
                            null)),

                    Arguments.of(secondRouteDefinition, new SwaggerUrl(secondRouteDefinition.getId(),
                            SwaggerMapper.getApiDocsUrl(secondRouteDefinition.getId()),
                            null))
            );
        }

        private static Stream<Arguments> isValidDataSource() {
            RouteDefinition firstRouteDefinition = new RouteDefinition();
                            firstRouteDefinition.setId("my-service");

            RouteDefinition secondRouteDefinition = new RouteDefinition();
                            secondRouteDefinition.setId("my-super-service");

            RouteDefinition thirdRouteDefinition = new RouteDefinition();
                            thirdRouteDefinition.setId("service");

            RouteDefinition forthRouteDefinition = new RouteDefinition();
                            forthRouteDefinition.setId("");

            RouteDefinition fifthRouteDefinition = new RouteDefinition();

            return Stream.of(
                    Arguments.of(firstRouteDefinition, true),
                    Arguments.of(secondRouteDefinition, true),
                    Arguments.of(thirdRouteDefinition, false),
                    Arguments.of(forthRouteDefinition, false),
                    Arguments.of(fifthRouteDefinition, false)
            );
        }
    }

    @Nested
    @DisplayName("Failure")
    class Failure {

        @ParameterizedTest
        @MethodSource("isValidDataSource")
        @DisplayName("isValid(): Should return false")
        void isValid(final RouteDefinition routeDefinition) {
            // Given
            // When
            // Then
            assertThat(SwaggerMapper.isValid(routeDefinition)).isFalse();
        }

        @ParameterizedTest
        @MethodSource("mapDataSource")
        @DisplayName("map(): Should throw exception")
        void mapWithException(final RouteDefinition routeDefinition) {
            // Given
            // When
            // Then
            Assertions.assertThatCode(() -> SwaggerMapper.map(routeDefinition))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Route definition cannot be null and blank");
        }

        @ParameterizedTest
        @MethodSource("getApiDocsUrlDataSource")
        @DisplayName("getApiDocsUrl(): Should throw exception")
        void getApiDocsUrl(final String serviceId) {
            // Given
            // When
            // Then
            Assertions.assertThat(catchThrowable(() -> SwaggerMapper.getApiDocsUrl(serviceId)))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Service Id cannot be null and blank");
        }

        private static Stream<Arguments> isValidDataSource() {
            RouteDefinition firstRouteDefinition = new RouteDefinition();
                            firstRouteDefinition.setId("");

            RouteDefinition secondRouteDefinition = new RouteDefinition();

            return Stream.of(
                    Arguments.of(firstRouteDefinition),
                    Arguments.of(secondRouteDefinition)
            );
        }

        private static Stream<Arguments> getApiDocsUrlDataSource() {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of((String) null)
            );
        }

        private static Stream<Arguments> mapDataSource() {
            RouteDefinition firstRouteDefinition = new RouteDefinition();

            RouteDefinition secondRouteDefinition = new RouteDefinition();
                            secondRouteDefinition.setId("");

            return Stream.of(
                    Arguments.of(firstRouteDefinition),
                    Arguments.of(secondRouteDefinition));
        }
    }
}