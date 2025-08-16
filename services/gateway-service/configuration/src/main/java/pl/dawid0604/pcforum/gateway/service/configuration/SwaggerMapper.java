package pl.dawid0604.pcforum.gateway.service.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Utility class for mapping Spring Cloud Gateway route
 * definitions to {@link SwaggerUrl}.
 *
 * <p>
 *     This mapper provides functionality to filter and
 *     transform route definitions from the service registry
 *     into appropriate Swagger UI URL objects. It implements
 *     specific business logic for identifying valid microservices
 *     and constructing their corresponding API documentation
 *     endpoints.
 * </p>
 *
 * @see RouteDefinition
 * @see SwaggerUrl
 */
final class SwaggerMapper {
    /**
     * Base path for OpenAPI 3.0 documentation endpoints.
     */
    private static final String API_DOCS = "/v3/api-docs/";

    /**
     * Pattern for identifying valid microservice route IDs.
     */
    private static final Pattern SERVICE_PATTERN = Pattern.compile(".+-service");

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private SwaggerMapper() { }

    /**
     * Determines whether a route definition represents
     * a valid Swagger documentation.
     *
     * <p>
     *     <strong>A route definition is considered valid when:</strong>
     *     <ul>
     *         <li>
     *             The route definition object is not null.
     *         </li>
     *
     *         <li>
     *             The route ID is not null or blank.
     *         </li>
     *
     *         <li>
     *             The route ID matches the pattern {@link SwaggerMapper#SERVICE_PATTERN}.
     *         </li>
     *     </ul>
     * </p>
     * @param routeDefinition the route definition to validate, may be null.
     * @return {@code true} if the route represents a valid microservice, {@code false} otherwise.
     */
    @SuppressWarnings("PMD.CommentDefaultAccessModifier")
    static boolean isValid(final RouteDefinition routeDefinition) {
        return Optional.ofNullable(routeDefinition)
                       .map(RouteDefinition::getId)
                       .filter(StringUtils::isNotBlank)
                       .filter(id -> SERVICE_PATTERN.matcher(id).matches())
                       .isPresent();
    }

    /**
     * Maps a route definition to a {@link SwaggerUrl}.
     *
     * <p>
     *     It creates a {@link SwaggerUrl} instance using the route
     *     ID as both the display name and service ID, with the API
     *     documentation URL constructed according to the standard
     *     OpenAPI 3.0 path convention.
     * </p>
     * @param routeDefinition the route definition to map, must not be null
     *                        and must have a valid ID.
     * @throws IllegalArgumentException if the route definition is null or has
     *                                  a blank ID.
     * @return a {@link SwaggerUrl} object ready for Swagger UI configuration.
     * @see SwaggerUrl
     * @see StringUtils
     * @see RouteDefinition
     * @see #getApiDocsUrl(String)
     */
    @SuppressWarnings("PMD.CommentDefaultAccessModifier")
    static SwaggerUrl map(final RouteDefinition routeDefinition) {
        return Optional.ofNullable(routeDefinition)
                       .map(RouteDefinition::getId)
                       .filter(StringUtils::isNotBlank)
                       .map(id -> new SwaggerUrl(id, getApiDocsUrl(id), null))
                       .orElseThrow(() -> new IllegalArgumentException("Route definition cannot be null and blank"));
    }

    /**
     * Constructs the API documentation URL for a given service ID.
     *
     * <p>
     *     Transforms a service ID by removing the {@code "-service"}
     *     suffix and prepending the standard OpenAPI 3.0 documentation
     *     path.
     *     <strong>For example:</strong>
     *     <ul>
     *         <li>
     *             {@code "user-service"} -> {@code "/v3/api-docs/user"}
     *         </li>
     *
     *         <li>
     *             {@code "order-service"} -> {@code "/v3/api-docs/order"}
     *         </li>
     *     </ul>
     * </p>
     * @param serviceId the service ID, must not be null or blank.
     * @throws IllegalArgumentException if the service ID is null or blank.
     * @return the constructed API documentation URL.
     *
     * @see StringUtils
     * @see #API_DOCS
     */
    @SuppressWarnings("PMD.CommentDefaultAccessModifier")
    static String getApiDocsUrl(final String serviceId) {
        return Optional.ofNullable(serviceId)
                       .filter(StringUtils::isNotBlank)
                       .map(id -> id.replace("-service", EMPTY))
                       .map(id -> API_DOCS + id)
                       .orElseThrow(() -> new IllegalArgumentException("Service Id cannot be null and blank"));
    }
}
