package pl.dawid0604.pcforum.gateway.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;
import static pl.dawid0604.pcforum.gateway.service.configuration.Constants.ROUTES_CACHE;

/**
 * Service responsible for managing and caching route
 * definitions for Swagger documentation.
 * <p>
 *     This service retrieves route definitions for the
 *     underlying locator and provides caching functionality
 *     to improve performance and reduce load on the route
 *     registry.
 * </p>
 *
 * <p>
 *     The service uses Spring Cache with Caffeine as
 *     the underlying cache implementation.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = PACKAGE)
class SwaggerRouteService {

    /**
     * Interface responsible for managing Eureka route definitions.
     */
    private final RouteDefinitionLocator locator;

    /**
     * Timeout in seconds for blocking operations when
     * fetching definitions.
     */
    private static final int BLOCKAGE_TIMEOUT = 5;

    /**
     * Retrieves all available route definitions with caching support.
     * <p>
     *     This method fetches route definitions from the underlying
     *     {@link RouteDefinitionLocator} and caches the results.
     * </p>
     *
     * <p>
     *     <strong>Cache behaviour:</strong>
     *     <ul>
     *         <li>
     *             Cache name: {@value Constants#ROUTES_CACHE}
     *         </li>
     *
     *         <li>
     *             Cache is bypassed if result is null or empty list.
     *         </li>
     *
     *         <li>
     *             Cache entities are automatically evicted when expired
     *         </li>
     *     </ul>
     * </p>
     * @return List of route definitions available in the system.
     *
     * @see RouteDefinitionLocator#getRouteDefinitions()
     * @see Cacheable
     */
    @Cacheable(value = ROUTES_CACHE, unless = "#result == null || #result.isEmpty()")
    public List<RouteDefinition> getRouteDefinitions() {
        log.debug("Fetching route definitions from source (cache missing)");
        return locator.getRouteDefinitions()
                      .collectList()
                      .block(Duration.ofSeconds(BLOCKAGE_TIMEOUT));
    }
}
