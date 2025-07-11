package com.example.devopsapp.service;

import net.rubyeye.xmemcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private MemcachedClient memcachedClient;

    /**
     * Store a value in cache
     */
    public void put(String key, Object value, int expiration) {
        try {
            memcachedClient.set(key, expiration, value);
            logger.info("Cached value for key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to cache value for key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * Retrieve a value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            T value = (T) memcachedClient.get(key);
            if (value != null) {
                logger.info("Cache hit for key: {}", key);
            } else {
                logger.info("Cache miss for key: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("Failed to retrieve value for key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Remove a value from cache
     */
    public void delete(String key) {
        try {
            memcachedClient.delete(key);
            logger.info("Deleted cache entry for key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete cache entry for key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * Check if cache is working
     */
    public boolean isHealthy() {
        try {
            String testKey = "health_test_" + System.currentTimeMillis();
            String testValue = "test_value";
            
            // Test set operation
            memcachedClient.set(testKey, 60, testValue);
            
            // Test get operation
            String retrievedValue = memcachedClient.get(testKey);
            
            // Test delete operation
            memcachedClient.delete(testKey);
            
            return testValue.equals(retrievedValue);
        } catch (Exception e) {
            logger.error("Cache health check failed: {}", e.getMessage());
            return false;
        }
    }
}
