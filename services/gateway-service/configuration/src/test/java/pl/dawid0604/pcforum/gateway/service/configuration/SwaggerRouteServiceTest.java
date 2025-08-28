package pl.dawid0604.pcforum.gateway.service.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PACKAGE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NoArgsConstructor(access = PACKAGE)
class SwaggerRouteServiceTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit tests")
    class UnitTests {

        @Mock
        private RouteDefinitionLocator routeDefinitionLocator;

        @InjectMocks
        private SwaggerRouteService service;

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("Should get properly routes")
            void getRouteDefinitions() {
                // Given
                RouteDefinition firstRouteDefinition = createRoute("user-service");
                RouteDefinition secondDefinition = createRoute("order-service");

                given(routeDefinitionLocator.getRouteDefinitions())
                        .willReturn(Flux.just(firstRouteDefinition, secondDefinition));

                // When
                var result = service.getRouteDefinitions();

                // Then
                Assertions.assertThat(result)
                          .hasSize(2)
                        .extracting(RouteDefinition::getId)
                        .containsOnly("user-service", "order-service");
            }

            @Test
            @DisplayName("Should get properly empty routes")
            void getEmptyRouteDefinitions() {
                // Given
                given(routeDefinitionLocator.getRouteDefinitions())
                        .willReturn(Flux.empty());

                // When
                var result = service.getRouteDefinitions();

                // Then
                Assertions.assertThat(result)
                          .isEmpty();
            }
        }

        @Nested
        @DisplayName("Failure")
        class Failure {

            @Test
            @DisplayName("Should throw timeout exception")
            void getRouteDefinitions() {
                // Given
                given(routeDefinitionLocator.getRouteDefinitions())
                        .willReturn(Flux.never());

                // When
                // Then
                Assertions.assertThatCode(() -> service.getRouteDefinitions())
                          .isInstanceOf(IllegalStateException.class)
                          .hasMessageContaining("Timeout");
            }
        }
    }

    @Nested
    @DisplayName("Integration tests")
    @SpringBootTest(classes = {
            SwaggerRouteService.class,
            CacheConfig.class
    })
    class IntegrationTests {

        @MockitoBean
        @SuppressWarnings("unused")
        private RouteDefinitionLocator routeDefinitionLocator;

        @Autowired
        @SuppressWarnings("unused")
        private SwaggerRouteService service;

        @Autowired
        @SuppressWarnings("unused")
        private CacheManager cacheManager;

        @BeforeEach
        void clearCache() {
            Objects.requireNonNull(cacheManager.getCache(Constants.ROUTES_CACHE))
                                               .clear();
        }

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("Should get properly routes")
            void getRouteDefinitions() {
                // Given
                RouteDefinition firstRouteDefinition = createRoute("user-service");
                RouteDefinition secondDefinition = createRoute("order-service");

                given(routeDefinitionLocator.getRouteDefinitions())
                        .willReturn(Flux.just(firstRouteDefinition, secondDefinition));

                // When
                var firstCallResult = service.getRouteDefinitions();
                var secondCallResult = service.getRouteDefinitions();

                // Then
                Assertions.assertThat(firstCallResult).hasSize(2);
                Assertions.assertThat(secondCallResult).hasSize(2);

                // Only once is called
                verify(routeDefinitionLocator).getRouteDefinitions();
                Assertions.assertThat(cacheManager.getCache(Constants.ROUTES_CACHE))
                          .isNotNull()
                          .satisfies(cache -> {

                              @SuppressWarnings("unchecked")
                              var nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
                              Assertions.assertThat(nativeCache.asMap()).isNotEmpty();

                              @SuppressWarnings("unchecked")
                              var cachedList = (List<RouteDefinition>) nativeCache.asMap()
                                                                                  .values()
                                                                                  .stream()
                                                                                  .findFirst()
                                                                                  .orElseThrow();

                              Assertions.assertThat(cachedList)
                                        .hasSize(2)
                                        .extracting(RouteDefinition::getId)
                                        .containsExactly(firstRouteDefinition.getId(), secondDefinition.getId());
                          });
            }

            @Test
            @DisplayName("Should get properly empty routes")
            void getEmptyRouteDefinitions() {
                // Given
                given(routeDefinitionLocator.getRouteDefinitions())
                        .willReturn(Flux.empty());

                // When
                var firstCallResult = service.getRouteDefinitions();
                var secondCallResult = service.getRouteDefinitions();

                // Then
                Assertions.assertThat(firstCallResult).isEmpty();
                Assertions.assertThat(secondCallResult).isEmpty();

                // Only once is called
                verify(routeDefinitionLocator, times(2)).getRouteDefinitions();

                Assertions.assertThat(cacheManager.getCache(Constants.ROUTES_CACHE))
                          .isNotNull()
                          .extracting(org.springframework.cache.Cache::getNativeCache)
                          .extracting(Cache.class::cast)
                          .extracting(Cache::asMap)
                          .asInstanceOf(InstanceOfAssertFactories.MAP)
                          .isNullOrEmpty();
            }
        }
    }

    private static RouteDefinition createRoute(final String id) {
        RouteDefinition routeDefinition = new RouteDefinition();
                        routeDefinition.setId(id);

        return routeDefinition;
    }
}