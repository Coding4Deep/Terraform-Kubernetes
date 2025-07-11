package com.example.devopsapp.service;

import com.example.devopsapp.model.User;
import com.example.devopsapp.repository.UserRepository;
import com.example.devopsapp.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int CACHE_EXPIRATION = 300; // 5 minutes

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final CacheService cacheService;

    @Autowired
    public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate, CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.rabbitTemplate = rabbitTemplate;
        this.cacheService = cacheService;
    }

    public Optional<User> findByEmail(String email) {
        String cacheKey = "user:email:" + email;
        
        // Try to get from cache first
        User cachedUser = cacheService.get(cacheKey);
        if (cachedUser != null) {
            logger.info("User found in cache for email: {}", email);
            return Optional.of(cachedUser);
        }
        
        // If not in cache, get from database
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            // Cache the result
            cacheService.put(cacheKey, user.get(), CACHE_EXPIRATION);
            logger.info("User cached for email: {}", email);
        }
        
        return user;
    }

    public Optional<User> findByUsername(String username) {
        String cacheKey = "user:username:" + username;
        
        // Try to get from cache first
        User cachedUser = cacheService.get(cacheKey);
        if (cachedUser != null) {
            logger.info("User found in cache for username: {}", username);
            return Optional.of(cachedUser);
        }
        
        // If not in cache, get from database
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            // Cache the result
            cacheService.put(cacheKey, user.get(), CACHE_EXPIRATION);
            logger.info("User cached for username: {}", username);
        }
        
        return user;
    }

    public User saveUser(User user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Cache the new user
        if (savedUser.getEmail() != null) {
            cacheService.put("user:email:" + savedUser.getEmail(), savedUser, CACHE_EXPIRATION);
        }
        if (savedUser.getUsername() != null) {
            cacheService.put("user:username:" + savedUser.getUsername(), savedUser, CACHE_EXPIRATION);
        }

        // Send a message to RabbitMQ queue
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_QUEUE,
                "New user registered: " + savedUser.getUsername());

        logger.info("User saved and cached: {}", savedUser.getUsername());
        return savedUser;
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public List<User> getAllUsers() {
        String cacheKey = "users:all";
        
        // Try to get from cache first
        List<User> cachedUsers = cacheService.get(cacheKey);
        if (cachedUsers != null) {
            logger.info("All users found in cache");
            return cachedUsers;
        }
        
        // If not in cache, get from database
        List<User> users = userRepository.findAll();
        
        // Cache the result for a shorter time since this list changes frequently
        cacheService.put(cacheKey, users, 60); // 1 minute cache
        logger.info("All users cached");
        
        return users;
    }
}
