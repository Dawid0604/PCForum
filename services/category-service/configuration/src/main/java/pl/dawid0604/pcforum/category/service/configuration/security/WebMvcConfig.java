package pl.dawid0604.pcforum.category.service.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *     Configuration class to manage web interceptors and request processing.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@SuppressWarnings("unused")
class WebMvcConfig implements WebMvcConfigurer {

    /**
     * <p>
     * Rate limiting interceptor for API request throttling and
     * abuse prevention.
     * </p>
     */
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * <p>
     *     Configures and registers HTTP interceptors for request
     *     processing pipeline.
     * </p>
     *
     * <p>
     *     <strong>Request processing flow:</strong>
     *     <ol>
     *         <li>
     *             Incoming HTTP request matches path patterns.
     *         </li>
     *
     *         <li>
     *             {@link RateLimitInterceptor} checks request frequency.
     *         </li>
     *
     *         <li>
     *             If within limits, request proceeds to controller.
     *         </li>
     *
     *         <li>
     *             If rate exceeded, HTTP 429 (Too many requests) returned.
     *         </li>
     *     </ol>
     * </p>
     * @param registry to customize HTTP interceptors.
     * @see RateLimitInterceptor
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/v3/api-docs/**"
                );
    }
}
