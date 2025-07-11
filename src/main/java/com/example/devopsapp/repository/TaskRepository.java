package com.example.devopsapp.repository;

import com.example.devopsapp.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    
    List<Task> findByUserId(String userId);
    
    List<Task> findByUserIdAndStatus(String userId, String status);
    
    List<Task> findByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("{ 'userId': ?0, 'status': { $in: ?1 } }")
    List<Task> findByUserIdAndStatusIn(String userId, List<String> statuses);
    
    long countByUserId(String userId);
    
    long countByUserIdAndStatus(String userId, String status);
}
