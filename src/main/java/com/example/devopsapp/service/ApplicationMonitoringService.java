package com.example.devopsapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import net.rubyeye.xmemcached.MemcachedClient;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ApplicationMonitoringService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MemcachedClient memcachedClient;

    @Autowired
    private CacheService cacheService;

    public Map<String, Object> getApplicationHealth() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("application", getApplicationStatus());
        health.put("database", getDatabaseStatus());
        health.put("messageQueue", getMessageQueueStatus());
        health.put("cache", getCacheStatus());
        health.put("network", getNetworkStatus());
        
        return health;
    }

    private Map<String, Object> getApplicationStatus() {
        Map<String, Object> appStatus = new HashMap<>();
        
        try {
            // Application basic info
            appStatus.put("name", "DevOps Portal");
            appStatus.put("version", "1.0.0");
            appStatus.put("status", "UP");
            appStatus.put("port", "8080");
            
            // JVM Stats
            Runtime runtime = Runtime.getRuntime();
            appStatus.put("jvmMemoryUsed", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
            appStatus.put("jvmMemoryFree", formatBytes(runtime.freeMemory()));
            appStatus.put("jvmMemoryTotal", formatBytes(runtime.totalMemory()));
            appStatus.put("jvmMemoryMax", formatBytes(runtime.maxMemory()));
            
            // Thread info
            appStatus.put("activeThreads", Thread.activeCount());
            
        } catch (Exception e) {
            appStatus.put("status", "DOWN");
            appStatus.put("error", e.getMessage());
        }
        
        return appStatus;
    }

    private Map<String, Object> getDatabaseStatus() {
        Map<String, Object> dbStatus = new HashMap<>();
        
        try {
            // Test MongoDB connection
            mongoTemplate.getCollection("test").estimatedDocumentCount();
            
            dbStatus.put("type", "MongoDB");
            dbStatus.put("status", "UP");
            dbStatus.put("host", getMongoHost());
            dbStatus.put("database", mongoTemplate.getDb().getName());
            
            // Get collection stats
            dbStatus.put("collections", mongoTemplate.getCollectionNames().size());
            
        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
        }
        
        return dbStatus;
    }

    private Map<String, Object> getMessageQueueStatus() {
        Map<String, Object> mqStatus = new HashMap<>();
        
        try {
            // Test RabbitMQ connection
            rabbitTemplate.execute(channel -> {
                return channel.isOpen();
            });
            
            mqStatus.put("type", "RabbitMQ");
            mqStatus.put("status", "UP");
            mqStatus.put("host", getRabbitMQHost());
            
        } catch (Exception e) {
            mqStatus.put("status", "DOWN");
            mqStatus.put("error", e.getMessage());
        }
        
        return mqStatus;
    }

    private Map<String, Object> getCacheStatus() {
        Map<String, Object> cacheStatus = new HashMap<>();
        
        try {
            // Use CacheService for health check
            boolean isHealthy = cacheService.isHealthy();
            
            cacheStatus.put("type", "Memcached");
            cacheStatus.put("status", isHealthy ? "UP" : "DOWN");
            cacheStatus.put("testResult", isHealthy ? "SUCCESS" : "FAILED");
            
            // Additional direct test for monitoring
            memcachedClient.set("health_check", 60, "test");
            String result = memcachedClient.get("health_check");
            cacheStatus.put("directTest", result != null ? "SUCCESS" : "FAILED");
            
        } catch (Exception e) {
            cacheStatus.put("status", "DOWN");
            cacheStatus.put("error", e.getMessage());
        }
        
        return cacheStatus;
    }

    private Map<String, Object> getNetworkStatus() {
        Map<String, Object> networkStatus = new HashMap<>();
        
        try {
            // Check common ports
            networkStatus.put("port8080", isPortOpen("localhost", 8080) ? "OPEN" : "CLOSED");
            networkStatus.put("port27017", isPortOpen("localhost", 27017) ? "OPEN" : "CLOSED"); // MongoDB
            networkStatus.put("port5672", isPortOpen("localhost", 5672) ? "OPEN" : "CLOSED");   // RabbitMQ
            networkStatus.put("port11211", isPortOpen("localhost", 11211) ? "OPEN" : "CLOSED"); // Memcached
            
            networkStatus.put("status", "UP");
            
        } catch (Exception e) {
            networkStatus.put("status", "DOWN");
            networkStatus.put("error", e.getMessage());
        }
        
        return networkStatus;
    }

    private boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getMongoHost() {
        try {
            return System.getProperty("spring.data.mongodb.host", "localhost");
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getRabbitMQHost() {
        try {
            return System.getProperty("spring.rabbitmq.host", "localhost");
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public Map<String, Object> getDetailedServiceStatus() {
        Map<String, Object> serviceStatus = new HashMap<>();
        
        // MongoDB detailed status
        serviceStatus.put("mongodb", getMongoDBDetails());
        
        // RabbitMQ detailed status
        serviceStatus.put("rabbitmq", getRabbitMQDetails());
        
        // Memcached detailed status
        serviceStatus.put("memcached", getMemcachedDetails());
        
        return serviceStatus;
    }

    private Map<String, Object> getMongoDBDetails() {
        Map<String, Object> mongoDetails = new HashMap<>();
        
        try {
            mongoDetails.put("status", "UP");
            mongoDetails.put("database", mongoTemplate.getDb().getName());
            mongoDetails.put("collections", mongoTemplate.getCollectionNames());
            
            // Get document counts
            mongoDetails.put("userCount", mongoTemplate.getCollection("users").estimatedDocumentCount());
            mongoDetails.put("taskCount", mongoTemplate.getCollection("tasks").estimatedDocumentCount());
            mongoDetails.put("adminCount", mongoTemplate.getCollection("admins").estimatedDocumentCount());
            
        } catch (Exception e) {
            mongoDetails.put("status", "DOWN");
            mongoDetails.put("error", e.getMessage());
        }
        
        return mongoDetails;
    }

    private Map<String, Object> getRabbitMQDetails() {
        Map<String, Object> rabbitDetails = new HashMap<>();
        
        try {
            rabbitDetails.put("status", "UP");
            rabbitDetails.put("connectionOpen", rabbitTemplate.execute(channel -> channel.isOpen()));
            
        } catch (Exception e) {
            rabbitDetails.put("status", "DOWN");
            rabbitDetails.put("error", e.getMessage());
        }
        
        return rabbitDetails;
    }

    private Map<String, Object> getMemcachedDetails() {
        Map<String, Object> memcachedDetails = new HashMap<>();
        
        try {
            // Use CacheService for comprehensive health check
            boolean isHealthy = cacheService.isHealthy();
            
            memcachedDetails.put("status", isHealthy ? "UP" : "DOWN");
            memcachedDetails.put("healthCheck", isHealthy ? "PASSED" : "FAILED");
            
            // Additional detailed test operations
            String testKey = "admin_health_check_" + System.currentTimeMillis();
            memcachedClient.set(testKey, 60, "test_value");
            String retrieved = memcachedClient.get(testKey);
            memcachedClient.delete(testKey);
            
            memcachedDetails.put("testOperation", retrieved != null ? "SUCCESS" : "FAILED");
            memcachedDetails.put("cacheServiceIntegration", "ENABLED");
            
        } catch (Exception e) {
            memcachedDetails.put("status", "DOWN");
            memcachedDetails.put("error", e.getMessage());
            memcachedDetails.put("cacheServiceIntegration", "FAILED");
        }
        
        return memcachedDetails;
    }
}
