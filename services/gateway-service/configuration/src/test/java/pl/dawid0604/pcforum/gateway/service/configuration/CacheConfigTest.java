package pl.dawid0604.pcforum.gateway.service.configuration;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static lombok.AccessLevel.PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.dawid0604.pcforum.gateway.service.configuration.Constants.ROUTES_CACHE;

@NoArgsConstructor(access = PACKAGE)
@SpringBootTest(classes = CacheConfig.class)
class CacheConfigTest {

    @Autowired
    @SuppressWarnings("unused")
    private CacheManager cacheManager;

    @Test
    void shouldHaveRoutesCache() {
        assertThat(cacheManager.getCacheNames()).contains(ROUTES_CACHE);
    }

    @Test
    void shouldHaveCaffeineAsCacheManager() {
        assertThat(cacheManager.getCache(ROUTES_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(ROUTES_CACHE)).extracting(Cache::getNativeCache)
                                                       .isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);
    }

    @Test
    void shouldHaveStatsEnabled() {
        assertThat(cacheManager.getCache(ROUTES_CACHE)).isNotNull();
        assertThat(cacheManager.getCache(ROUTES_CACHE)).extracting(Cache::getNativeCache)
                                                       .extracting(c -> ((com.github.benmanes.caffeine.cache.Cache<?, ?>) c).stats())
                                                       .isNotNull();
    }
}