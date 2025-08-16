package pl.dawid0604.pcforum.gateway.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PACKAGE;

/**
 * The configuration class for dynamic Swagger UI URL
 * management.
 *
 * <p>
 *     This configuration automatically discovers and
 *     registers microservice endpoints for Swagger UI
 *     documentation. It fetches route definitions
 *     from the Eureka service registry and updates
 *     the Swagger UI configuration to include all
 *     available API documentation endpoints.
 * </p>
 */
@Slf4j
@Configuration
@SuppressWarnings({ "unused", "PMD.CommentDefaultAccessModifier" })
@RequiredArgsConstructor(access = PACKAGE)
class SwaggerConfig {

    /**
     * Class responsible for managing Swagger UI properties.
     */
    private final SwaggerUiConfigProperties swaggerProperties;

    /**
     * Service responsible for managing and caching route
     * definitions for Swagger documentation.
     */
    private final SwaggerRouteService routeService;

    /**
     * Interval between refresh cycles.
     */
    private static final int REFRESH_TIMEOUT = 30_000;

    /**
     * Scheduled task that refreshes Swagger UI URL's
     * every {@link #REFRESH_TIMEOUT} seconds.
     *
     * <p>
     *     This method runs in the background to ensure that
     *     the Swagger UI interface always reflects the current
     *     state of available microservices and their API
     *     documentation endpoints.
     * </p>
     *
     * @see Scheduled
     */
    @Scheduled(fixedDelay = REFRESH_TIMEOUT)
    public void refreshSwaggerUrls() {
        updateSwaggerUrls();
    }

    /**
     * Updates the Swagger UI configuration with current
     * microservice endpoints.
     *
     * <p>
     *     This method performs the following operations:
     *     <ol>
     *         <li>
     *             Retrieves all available route definitions
     *             from the service registry.
     *         </li>
     *
     *         <li>
     *             Filters routes to include only valid microservices
     *         </li>
     *
     *         <li>
     *             Maps route definitions to Swagger UI URL objects
     *             {@link SwaggerUrl}
     *         </li>
     *
     *         <li>
     *             Updates Swagger UI Config properties.
     *         </li>
     *     </ol>
     * </p>
     *
     * @see SwaggerMapper#isValid(org.springframework.cloud.gateway.route.RouteDefinition)
     * @see SwaggerMapper#map(org.springframework.cloud.gateway.route.RouteDefinition)
     * @see SwaggerRouteService
     */
    private void updateSwaggerUrls() {
        final Set<SwaggerUrl> urls = routeService.getRouteDefinitions()
                                                 .stream()
                                                 .filter(SwaggerMapper::isValid)
                                                 .map(SwaggerMapper::map)
                                                 .collect(toSet());

        swaggerProperties.setUrls(urls);
        log.info("Swagger URLs successfully refreshed");
    }
}
