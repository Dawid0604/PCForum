package pl.dawid0604.pcforum.gateway.service.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwaggerConfigTest {

    @Mock
    private SwaggerUiConfigProperties swaggerProperties;

    @Mock
    private SwaggerRouteService swaggerRouteService;

    @Captor
    private ArgumentCaptor<Set<SwaggerUrl>> urlCaptor;

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    @Test
    @DisplayName("Should update Swagger URLs")
    void shouldUpdateSwaggerUrls() {
        // Given
        RouteDefinition validDefinition = createRoute("my-service");
        RouteDefinition validDefinition2 = createRoute("my-super-service");
        RouteDefinition invalidDefinition = createRoute("service");
        RouteDefinition invalidDefinition2 = createRoute("");

        List<RouteDefinition> routeDefinitions = List.of(
                validDefinition, validDefinition2,
                invalidDefinition, invalidDefinition2
        );

        given(swaggerRouteService.getRouteDefinitions())
                .willReturn(routeDefinitions);

        try(MockedStatic<SwaggerMapper> mockedMapper = mockStatic(SwaggerMapper.class)) {
            mockedMapper.when(() -> SwaggerMapper.isValid(validDefinition)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.isValid(validDefinition2)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.isValid(invalidDefinition)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.isValid(invalidDefinition2)).thenCallRealMethod();

            mockedMapper.when(() -> SwaggerMapper.map(validDefinition)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.map(validDefinition2)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.map(invalidDefinition)).thenCallRealMethod();
            mockedMapper.when(() -> SwaggerMapper.map(invalidDefinition2)).thenCallRealMethod();

            // When
            swaggerConfig.refreshSwaggerUrls();

            // Then
            verify(swaggerProperties).setUrls(urlCaptor.capture());

            Assertions.assertThat(urlCaptor.getValue())
                      .hasSize(2)
                      .extracting(SwaggerUrl::getDisplayName)
                      .containsExactlyInAnyOrder(validDefinition.getId(), validDefinition2.getId());

            mockedMapper.verify(() -> SwaggerMapper.isValid(validDefinition));
            mockedMapper.verify(() -> SwaggerMapper.isValid(validDefinition2));
            mockedMapper.verify(() -> SwaggerMapper.isValid(invalidDefinition));
            mockedMapper.verify(() -> SwaggerMapper.isValid(invalidDefinition2));

            mockedMapper.verify(() -> SwaggerMapper.map(validDefinition));
            mockedMapper.verify(() -> SwaggerMapper.map(validDefinition2));
            mockedMapper.verify(() -> SwaggerMapper.map(invalidDefinition), never());
            mockedMapper.verify(() -> SwaggerMapper.map(invalidDefinition2), never());
        }
    }

    private static RouteDefinition createRoute(final String serviceId) {
        RouteDefinition routeDefinition = new RouteDefinition();
                        routeDefinition.setId(serviceId);

        return routeDefinition;
    }
}