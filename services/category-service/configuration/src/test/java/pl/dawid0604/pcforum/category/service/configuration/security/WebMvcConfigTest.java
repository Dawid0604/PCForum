package pl.dawid0604.pcforum.category.service.configuration.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        WebMvcConfig.class,
        RateLimitInterceptor.class
})
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class WebMvcConfigTest {

    @Autowired
    @SuppressWarnings("unused")
    private WebMvcConfig webMvcConfig;

    @Autowired
    @SuppressWarnings("unused")
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    @DisplayName("Should add RateLimitInterceptor")
    void shouldAddRateLimitInterceptor() {
        // Given
        final InterceptorRegistry registry = mock(InterceptorRegistry.class);
        final InterceptorRegistration registration = mock(InterceptorRegistration.class);

        when(registry.addInterceptor(any(RateLimitInterceptor.class)))
                .thenReturn(registration);

        when(registration.addPathPatterns(anyString()))
                .thenReturn(registration);

        when(registration.excludePathPatterns(anyString()))
                .thenReturn(registration);

        // When
        webMvcConfig.addInterceptors(registry);

        // Then
        verify(registry).addInterceptor(rateLimitInterceptor);
        verify(registration).addPathPatterns("/**");
        verify(registration).excludePathPatterns(
                "/actuator/**",
                "/v3/api-docs/**"
        );
    }
}