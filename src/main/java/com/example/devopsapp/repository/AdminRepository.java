package com.example.devopsapp.repository;

import com.example.devopsapp.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {
    
    Optional<Admin> findByEmail(String email);
    
    Optional<Admin> findByUsername(String username);
    
    List<Admin> findByActiveTrue();
    
    List<Admin> findByActiveFalse();
    
    @Query("{ 'active': true }")
    List<Admin> findAllActiveAdmins();
    
    long countByActiveTrue();
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
}
