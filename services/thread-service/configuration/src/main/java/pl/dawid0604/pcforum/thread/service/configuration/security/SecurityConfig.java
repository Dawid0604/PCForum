package pl.dawid0604.pcforum.thread.service.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * <p>
 *     Configuration class that configures application security.
 * </p>
 *
 * <p>
 *     {@link EnableWebSecurity} enables ability to customize application security.
 * </p>
 *
 * <p>
 *     <strong>{@link EnableMethodSecurity} enables usage of annotations such as:</strong>
 *     <ul>
 *        <li>
 *            <p>
 *                {@link org.springframework.security.access.prepost.PreAuthorize}
 *            </p>
 *            <strong>Example:</strong>
 *            {@code @PreAuthorize(hasAnyRole("USER", "ADMIN"))}
 *        </li>
 *     </ul>
 * </p>
 *
 * @see EnableWebSecurity
 * @see EnableMethodSecurity
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SuppressWarnings("unused")
class SecurityConfig {

    /**
     * <p>
     *     Gateway microservice instance URL.
     * </p>
     */
    private final String gatewayServiceInstanceUrl;

    /**
     * <p>
     *     It means how long, in seconds, the response from
     *     a pre-flight request can be cached by clients.
     * </p>
     */
    private static final long CORS_MAX_AGE = 3600L;

    /**
     * <p>
     *     Constructor that set {@link #gatewayServiceInstanceUrl} field
     *     as final with injected value via {@link Value}.
     * </p>
     * @param incomingGatewayServiceUrl value which is available under <b>application.yml</b> file.
     */
    SecurityConfig(
            @Value("${custom.gatewayServiceUrl}")
            final String incomingGatewayServiceUrl) {

        this.gatewayServiceInstanceUrl = incomingGatewayServiceUrl;
    }

    /**
     * <p>
     *     {@link Bean} that configures {@link SecurityFilterChain} via {@link HttpSecurity}.
     * </p>
     *
     * <p>
     *     It uses {@link SessionCreationPolicy#STATELESS} session management due to
     *     use JWT tokens.
     * </p>
     *
     * <p>
     *     The {@link CustomJwtAuthenticationConverter} is used as JWT authentication converter.
     * </p>
     *
     * <p>
     *     Cors are enabled and configured via {@link #corsConfigurationSource()} method.
     * </p>
     *
     * <p>
     *     CSRF is currently disabled.
     * </p>
     *
     * <p>
     *     Additional headers are configured via {@link #customize(HeadersConfigurer)} method.
     * </p>
     * @param httpSecurity to configure security. It cannot be null.
     * @return configured {@link SecurityFilterChain} object.
     * @throws Exception when something goes wrong.
     * @see HttpSecurity
     * @see SessionCreationPolicy#STATELESS
     * @see #customize(HeadersConfigurer)
     * @see #customize(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry)
     */
    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        final CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter(
                new CustomKeycloakRoleConverter()
        );

        return httpSecurity.authorizeHttpRequests(SecurityConfig::customize)
                           .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                           .oauth2ResourceServer(c -> c.jwt(j -> j.jwtAuthenticationConverter(converter)))
                           .cors(c -> c.configurationSource(corsConfigurationSource()))
                           .csrf(AbstractHttpConfigurer::disable)
                           .headers(SecurityConfig::customize)
                           .build();
    }

    /**
     * <p>
     *     {@link Bean} to configure cors.
     * </p>
     *
     * <p>
     *     It uses {@link #gatewayServiceInstanceUrl} as value for
     *     {@link CorsConfiguration#setAllowedOriginPatterns(List)}.
     * </p>
     *
     * <p>
     *     The Allowed Headers, Exposed Headers and allowed methods are restricted.
     * </p>
     * @return configured {@link CorsConfigurationSource} object.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
                                configuration.setAllowedOriginPatterns(List.of(gatewayServiceInstanceUrl));
                                configuration.setAllowCredentials(false);
                                configuration.setMaxAge(CORS_MAX_AGE);

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Cache-Control"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-RateLimit-Limit",
                "X-Rate-Limit-Remaining",
                "X-Rate-Limit-Reset",
                "Retry-After"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT",
                "DELETE", "PATCH", "OPTIONS"
        ));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                                              source.registerCorsConfiguration("/category/**", configuration);

        return source;
    }

    /**
     * <p>
     *     Method to customize additional {@link HttpSecurity} headers.
     * </p>
     * @param configurer to customize headers. It cannot be null.
     */
    private static void customize(@NonNull final HeadersConfigurer<HttpSecurity> configurer) {
        configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                  .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                  .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin"))
                  .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                          "geolocation=(), microphone=(), camera=(), payment=(), usb=()"))
                  .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy",
                          "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"));
    }

    /**
     * <p>
     *     Method to customize endpoints and their accessibility.
     * </p>
     *
     * @param registry to customize request registry. It cannot be null.
     */
    private static void customize(
            @NonNull
            final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {

        registry.requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/category").permitAll()
                .requestMatchers(HttpMethod.GET, "/user").permitAll()
                .requestMatchers(HttpMethod.GET, "/match").permitAll()
                .requestMatchers(HttpMethod.GET, "/details/{publicId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/count/{categoryId}").permitAll()
                .anyRequest()
                .authenticated();
    }
}
