package com.tvg.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "tvg.multicache")
public class MultiCacheProperties {

    private String enableRedis;
    private String enableCaffeine;

    private Map<String, RedisCacheProperties> redis;
    private Map<String, CaffeineCacheProperties> caffeine;
    /*
    Example application.properties:
    tvg.multicache.enable-redis=true
    tvg.multicache.enable-caffeine=true
    tvg.multicache.redis.my-redis-cache=time-to-live=2h
    tvg.multicache.redis.my-other-redis-cache.use-key-prefix=true
    tvg.multicache.redis.my-other-redis-cache.key-prefix=super_tvg
    tvg.multicache.caffeine.my-caffeine-cache.spec=expireAfterAccess=30s,recordStats
     */

    public String getEnableRedis() { return enableRedis; }

    public void setEnableRedis(String enableRedis) { this.enableRedis = enableRedis; }

    public String getEnableCaffeine() { return enableCaffeine; }

    public void setEnableCaffeine(String enableCaffeine) { this.enableCaffeine = enableCaffeine; }

    public Map<String, RedisCacheProperties> getRedis() {
        return redis;
    }

    public void setRedis(Map<String, RedisCacheProperties> redis) {
        this.redis = redis;
    }

    public Map<String, CaffeineCacheProperties> getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(Map<String, CaffeineCacheProperties> caffeine) {
        this.caffeine = caffeine;
    }

    public static class RedisCacheProperties {

        private Duration timeToLive = Duration.ZERO;
        private boolean cacheNullValues = true;
        private String keyPrefix; //default assigned in CacheConfiguration
        private boolean useKeyPrefix = true;

        public RedisCacheProperties() {
        }

        public Duration getTimeToLive() {
            return this.timeToLive;
        }

        public void setTimeToLive(Duration timeToLive) {
            this.timeToLive = timeToLive;
        }

        public boolean isCacheNullValues() {
            return this.cacheNullValues;
        }

        public void setCacheNullValues(boolean cacheNullValues) {
            this.cacheNullValues = cacheNullValues;
        }

        public String getKeyPrefix() {
            return this.keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public boolean isUseKeyPrefix() {
            return this.useKeyPrefix;
        }

        public void setUseKeyPrefix(boolean useKeyPrefix) {
            this.useKeyPrefix = useKeyPrefix;
        }

        @Override
        public String toString() {
            return "RedisCacheProperties{" +
                    "timeToLive=" + timeToLive +
                    ", cacheNullValues=" + cacheNullValues +
                    ", keyPrefix='" + keyPrefix + '\'' +
                    ", useKeyPrefix=" + useKeyPrefix +
                    '}';
        }
    }

    public static class CaffeineCacheProperties {
        private String spec;

        public CaffeineCacheProperties() {
        }

        public String getSpec() { return this.spec; }

        public void setSpec(String spec) { this.spec = spec; }

        @Override
        public String toString() {
            return "CaffeineCacheProperties{" +
                    "spec=" + spec +
                    '}';
        }
    }
}
