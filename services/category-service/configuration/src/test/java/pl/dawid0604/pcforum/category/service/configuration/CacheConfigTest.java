package pl.dawid0604.pcforum.category.service.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import pl.dawid0604.pcforum.category.service.commons.Constants;

@SpringBootTest(classes = CacheConfig.class)
@TestPropertySource(properties = "spring.cloud.config.enabled=false")
class CacheConfigTest {

    @Autowired
    @SuppressWarnings("unused")
    private CacheManager cacheManager;

    @Test
    void shouldHaveCache() {
        // Given
        // When
        // Then
        Assertions.assertThat(cacheManager.getCacheNames())
                  .contains(Constants.CACHE_KEY);
    }

    @Test
    void shouldHaveCaffeineAsCacheManager() {
        // Given
        // When
        // Then
        Assertions.assertThat(cacheManager.getCache(Constants.CACHE_KEY))
                  .isNotNull()
                  .extracting(Cache::getNativeCache)
                  .isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);
    }

    @Test
    void shouldHaveStatsEnabled() {
        // Given
        // When
        // Then
        Assertions.assertThat(cacheManager.getCache(Constants.CACHE_KEY))
                  .isNotNull()
                  .extracting(Cache::getNativeCache)
                  .extracting(c -> ((com.github.benmanes.caffeine.cache.Cache<?, ?>) c).stats())
                  .isNotNull();
    }
}