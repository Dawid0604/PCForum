package pl.dawid0604.pcforum.frontend.service.configuration;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static lombok.AccessLevel.PACKAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.dawid0604.pcforum.frontend.service.configuration.CacheConfig.REFERENCED_CACHE_NAME;
import static pl.dawid0604.pcforum.frontend.service.configuration.CacheConfig.USER_CACHE_NAME;

@NoArgsConstructor(access = PACKAGE)
@SpringBootTest(classes = CacheConfig.class)
class CacheConfigTest {

    @Autowired
    @SuppressWarnings("unused")
    private CacheManager cacheManager;

    @Test
    void shouldHaveRequiredCaches() {
        assertThat(cacheManager.getCacheNames()).contains(USER_CACHE_NAME);
        assertThat(cacheManager.getCacheNames()).contains(REFERENCED_CACHE_NAME);
    }

    @Test
    void shouldHaveCaffeineAsCacheManager() {
        assertThat(cacheManager.getCache(USER_CACHE_NAME)).isNotNull();
        assertThat(cacheManager.getCache(USER_CACHE_NAME)).extracting(Cache::getNativeCache)
                                                          .isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);

        assertThat(cacheManager.getCache(REFERENCED_CACHE_NAME)).isNotNull();
        assertThat(cacheManager.getCache(REFERENCED_CACHE_NAME)).extracting(Cache::getNativeCache)
                                                                .isInstanceOf(com.github.benmanes.caffeine.cache.Cache.class);
    }

    @Test
    void shouldHaveStatsEnabled() {
        assertThat(cacheManager.getCache(USER_CACHE_NAME)).isNotNull();
        assertThat(cacheManager.getCache(USER_CACHE_NAME)).extracting(Cache::getNativeCache)
                                                          .extracting(c -> ((com.github.benmanes.caffeine.cache.Cache<?, ?>) c).stats())
                                                          .isNotNull();

        assertThat(cacheManager.getCache(REFERENCED_CACHE_NAME)).isNotNull();
        assertThat(cacheManager.getCache(REFERENCED_CACHE_NAME)).extracting(Cache::getNativeCache)
                                                                .extracting(c -> ((com.github.benmanes.caffeine.cache.Cache<?, ?>) c).stats())
                                                                .isNotNull();
    }
}