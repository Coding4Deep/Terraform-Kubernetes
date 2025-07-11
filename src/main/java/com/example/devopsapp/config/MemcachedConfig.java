package com.example.devopsapp.config;

import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Configuration
@EnableCaching
public class MemcachedConfig {

    private static final Logger logger = LoggerFactory.getLogger(MemcachedConfig.class);

    // Inject memcached servers from application.properties
    @Value("${memcached.servers}")
    private String memcachedServers;

    @Bean
    public MemcachedClient memcachedClient() throws IOException {
        logger.info("Configuring Memcached client with servers: {}", memcachedServers);
        
        try {
            // Parse the servers string (format: host:port)
            String[] serverParts = memcachedServers.split(":");
            String host = serverParts[0];
            int port = serverParts.length > 1 ? Integer.parseInt(serverParts[1]) : 11211;
            
            logger.info("Connecting to Memcached at {}:{}", host, port);
            
            XMemcachedClient client = new XMemcachedClient(host, port);
            
            // Set connection timeout
            client.setConnectTimeout(5000);
            client.setOpTimeout(3000);
            
            logger.info("Memcached client configured successfully");
            return client;
            
        } catch (Exception e) {
            logger.error("Failed to configure Memcached client: {}", e.getMessage());
            throw new IOException("Could not connect to Memcached server: " + memcachedServers, e);
        }
    }

    @Bean
    public CacheManager cacheManager() {
        return new SimpleCacheManager();
    }
}
