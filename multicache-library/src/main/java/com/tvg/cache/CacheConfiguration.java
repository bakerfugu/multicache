package com.tvg.cache;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(MultiCacheProperties.class)
public class CacheConfiguration {

    private MultiCacheProperties cacheProperties;
    @Value("${spring.application.name}")
    private String appName;

    final Logger log = LoggerFactory.getLogger(this.getClass());
    public CacheConfiguration(MultiCacheProperties cacheProperties
    ) {
        this.cacheProperties = cacheProperties;
        checkForCacheEnabledConsistency();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tvg.multicache", value = "enable-redis", havingValue = "true")
    public List <Cache> redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info(String.format("MultiCache: Enabling redis caches"));
        List <Cache> caches = new ArrayList<>();


        cacheProperties.getRedis().forEach( (name, properties) -> {
            log.debug(String.format("MultiCache: redis cache name %s props %s", name, properties));

            RedisCacheManager cache = RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(cacheConfiguration(name, properties))
                    .transactionAware()
                    .build();

            Cache redisCache = cache.getCache(name);
            //cacheMetricsRegistrar.bindCacheToRegistry(redisCache);
            caches.add(redisCache);
        });

        return caches;
    }

    private RedisCacheConfiguration cacheConfiguration(String cacheName, MultiCacheProperties.RedisCacheProperties properties) {
        String prefix;
        if (properties.getKeyPrefix() != null && properties.isUseKeyPrefix()) {
            prefix = appName + "-" + properties.getKeyPrefix() + "::";
        } else {
            prefix = appName + "-" + cacheName + "::";
        }

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getTimeToLive())
                .prefixKeysWith(prefix);


        if (!properties.isUseKeyPrefix()) {
            cacheConfig.disableCachingNullValues();
        }
        if (!properties.isUseKeyPrefix()) {
            cacheConfig.disableKeyPrefix();
        }

        return cacheConfig;
    }

    @Bean
    @ConditionalOnProperty(prefix = "tvg.multicache", value = "enable-caffeine", havingValue = "true")
    public List <Cache> caffeineCacheManager() {
        List <Cache> caches = new ArrayList<>();
        log.info(String.format("MultiCache: Enabling caffeine caches"));
        cacheProperties.getCaffeine().forEach( (name, properties) -> {
            log.debug(String.format("MultiCache: caffeine cache name %s props %s", name, properties));
            CaffeineCacheManager cache = new CaffeineCacheManager(name);
            cache.setCaffeineSpec(CaffeineSpec.parse(properties.getSpec()));

            caches.add(cache.getCache(name));
        });

        return caches;
    }

    @Bean
    public CacheManager cacheManager(List<Cache> caffeineCacheManager, List<Cache> redisCacheManager) {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();

        List<Cache> caches = new ArrayList<>();
        caches.addAll(caffeineCacheManager);
        caches.addAll(redisCacheManager);

        simpleCacheManager.setCaches(caches);
        return simpleCacheManager;
    }

//    @Bean
//    public CacheResolver cacheResolver() {
//        return new MultiCacheResolver(cacheManager(), this.cacheProperties);
//    }


    private void checkForCacheEnabledConsistency() {
        boolean redisCachesExist = cacheProperties.getRedis() != null && cacheProperties.getRedis().size() > 0;
        boolean caffeineCachesExist = cacheProperties.getCaffeine() != null && cacheProperties.getCaffeine().size() > 0;
        String redisEnabledString = cacheProperties.getEnableRedis();
        boolean redisEnabled = redisEnabledString != null && "true".equals(redisEnabledString);
        String caffeineEnabledString = cacheProperties.getEnableCaffeine();
        boolean caffeineEnabled = caffeineEnabledString != null && "true".equals(caffeineEnabledString);

        if (!redisEnabled && !redisCachesExist) {
            log.info("MultiCache: No redis caches created");
        }
        else if (!redisEnabled && redisCachesExist) {
            throw new RuntimeException("MultiCache: redis cache properties are configured in application.properties but redis caches are not enabled. Make sure tvg.multicache.enable-redis=true");
        }
        else if (redisEnabled && !redisCachesExist) {
            throw new RuntimeException("MultiCache: redis caches are enabled, but no redis caches are defined in application.properties");
        }

        if (!caffeineEnabled && !caffeineCachesExist) {
            log.info("MultiCache: No caffeine caches created");
        }
        else if (!caffeineEnabled && caffeineCachesExist) {
            throw new RuntimeException("MultiCache: caffeine cache properties are configured in application.properties but caffeine caches are not enabled. Make sure tvg.multicache.enable-caffeine=true");
        }
        else if (caffeineEnabled && !caffeineCachesExist) {
            throw new RuntimeException("MultiCache: caffeine caches are enabled, but no caffeine caches are defined in application.properties");
        }

        if (redisCachesExist && caffeineCachesExist) {
            Set<String> s = new HashSet<>(cacheProperties.getRedis().keySet());
            s.retainAll(cacheProperties.getCaffeine().keySet());
            if (s.size() > 0) {
                throw new RuntimeException("MultiCache: Cache name(s) " + s.toString() + " are defined with different cache types in application.properties");
            }
        }
    }
}