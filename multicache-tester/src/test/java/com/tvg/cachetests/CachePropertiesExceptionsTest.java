package com.tvg.cachetests;

import com.tvg.cache.MultiCacheProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CachePropertiesExceptionsTest {

    static MultiCacheProperties cacheProperties = Mockito.mock(MultiCacheProperties.class);

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @TestConfiguration
    static class TDSDatabaseBefore {

        @Bean
        public MultiCacheProperties cacheProperties() {
            return cacheProperties;
        }
    }

    @Before
    public void setUp() {
        log.info("Resetting cache properties");
        when(cacheProperties.getEnableCaffeine()).thenReturn("false");
        when(cacheProperties.getEnableRedis()).thenReturn("false");
        when(cacheProperties.getCaffeine()).thenReturn(null);
        when(cacheProperties.getRedis()).thenReturn(null);
    }

    @Test
    public void whenCaffeineEnabledButNoCaches_thenThrowException() throws Exception {
        when(cacheProperties.getEnableCaffeine()).thenReturn("true");

        SpringApplication springApplication1 = new SpringApplication(SpringCrudDemoApplication.class);

        assertThatThrownBy(() -> {
            springApplication1.run();
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("MultiCache: caffeine caches are enabled, but no caffeine caches are defined in application.properties");
    }

    @Test
    public void whenCaffeineDisabledButCachesExist_thenThrowException() throws Exception {
        Map<String, MultiCacheProperties.CaffeineCacheProperties> caffeine = new HashMap<>();
        MultiCacheProperties.CaffeineCacheProperties caffeineCacheProperties = new MultiCacheProperties.CaffeineCacheProperties();
        caffeineCacheProperties.setSpec("expireAfterAccess=50s,recordStats");
        caffeine.put("friend-list", caffeineCacheProperties);

        when(cacheProperties.getEnableCaffeine()).thenReturn("false");
        when(cacheProperties.getCaffeine()).thenReturn(caffeine);

        SpringApplication springApplication2 = new SpringApplication(SpringCrudDemoApplication.class);

        assertThatThrownBy(() -> {
            springApplication2.run();
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("MultiCache: caffeine cache properties are configured in application.properties" +
                        " but caffeine caches are not enabled");
    }

    @Test
    public void whenRedisEnabledButNoCaches_thenThrowException() throws Exception {
        when(cacheProperties.getEnableRedis()).thenReturn("true");

        SpringApplication springApplication3 = new SpringApplication(SpringCrudDemoApplication.class);

        assertThatThrownBy(() -> {
            springApplication3.run();
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("MultiCache: redis caches are enabled, but no redis caches are defined in application.properties");
    }

    @Test
    public void whenRedisDisabledButCachesExist_thenThrowException() throws Exception {
        Map<String, MultiCacheProperties.RedisCacheProperties> redis = new HashMap<>();
        MultiCacheProperties.RedisCacheProperties redisCacheProperties = new MultiCacheProperties.RedisCacheProperties();
        redisCacheProperties.setKeyPrefix("foo-bar");
        redis.put("contacts", redisCacheProperties);

        when(cacheProperties.getEnableRedis()).thenReturn("false");
        when(cacheProperties.getRedis()).thenReturn(redis);

        SpringApplication springApplication4 = new SpringApplication(SpringCrudDemoApplication.class);

        assertThatThrownBy(() -> {
            springApplication4.run();
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("MultiCache: redis cache properties are configured in application.properties but " +
                        "redis caches are not enabled");
    }

    @Test
    public void whenRedisAndCaffeineCachesHaveSameName_thenThrowException() throws Exception {
        String duplicateName = "duplicate-name";

        Map<String, MultiCacheProperties.RedisCacheProperties> redis = new HashMap<>();
        MultiCacheProperties.RedisCacheProperties redisCacheProperties = new MultiCacheProperties.RedisCacheProperties();
        redisCacheProperties.setKeyPrefix("foo-bar");
        redis.put(duplicateName, redisCacheProperties);

        when(cacheProperties.getEnableRedis()).thenReturn("true");
        when(cacheProperties.getRedis()).thenReturn(redis);

        Map<String, MultiCacheProperties.CaffeineCacheProperties> caffeine = new HashMap<>();
        MultiCacheProperties.CaffeineCacheProperties caffeineCacheProperties = new MultiCacheProperties.CaffeineCacheProperties();
        caffeineCacheProperties.setSpec("expireAfterAccess=50s,recordStats");
        caffeine.put(duplicateName, caffeineCacheProperties);

        when(cacheProperties.getEnableCaffeine()).thenReturn("true");
        when(cacheProperties.getCaffeine()).thenReturn(caffeine);

        SpringApplication springApplication5 = new SpringApplication(SpringCrudDemoApplication.class);

        assertThatThrownBy(() -> {
            springApplication5.run();
        }).hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("MultiCache: Cache name(s) [" + duplicateName +
                        "] are defined with different cache types in application.properties");
    }

    @Test
    public void whenRedisEnabledButNotCaffeine_thenOnlyRedisBeansExist() throws Exception {
        Map<String, MultiCacheProperties.RedisCacheProperties> redis = new HashMap<>();
        MultiCacheProperties.RedisCacheProperties redisCacheProperties = new MultiCacheProperties.RedisCacheProperties();
        redisCacheProperties.setKeyPrefix("foo-bar");
        redis.put("contacts", redisCacheProperties);

        when(cacheProperties.getEnableRedis()).thenReturn("true");
        when(cacheProperties.getRedis()).thenReturn(redis);

        SpringApplication springApplication = new SpringApplication(SpringCrudDemoApplication.class);
        springApplication.run();

        //springApplication.get

        //get beans?
    }
}
