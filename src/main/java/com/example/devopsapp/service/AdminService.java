package com.example.devopsapp.service;

import com.example.devopsapp.model.Admin;
import com.example.devopsapp.model.User;
import com.example.devopsapp.model.Task;
import com.example.devopsapp.repository.AdminRepository;
import com.example.devopsapp.repository.UserRepository;
import com.example.devopsapp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdminRepository adminRepository, 
                       UserRepository userRepository,
                       TaskRepository taskRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin Management
    public Admin saveAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setActive(true); // Set admin as active
        return adminRepository.save(admin);
    }

    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public List<Admin> getActiveAdmins() {
        return adminRepository.findByActiveTrue();
    }

    public void updateLastLogin(String adminId) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);
        }
    }

    // User Management for Admins
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            // Delete all tasks for this user first
            List<Task> userTasks = taskRepository.findByUserId(userId);
            taskRepository.deleteAll(userTasks);
            
            // Delete the user
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Task Management for Admins
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByUserId(String userId) {
        return taskRepository.findByUserId(userId);
    }

    public long getTotalTasksCount() {
        return taskRepository.count();
    }

    public long getTasksCountByStatus(String status) {
        return taskRepository.findAll().stream()
                .filter(task -> status.equals(task.getStatus()))
                .count();
    }

    public boolean deleteTask(String taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public Optional<Task> getTaskById(String taskId) {
        return taskRepository.findById(taskId);
    }

    public Task updateTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    // Statistics for Admin Dashboard
    public AdminStats getAdminStats() {
        AdminStats stats = new AdminStats();
        stats.setTotalUsers(getTotalUsersCount());
        stats.setTotalTasks(getTotalTasksCount());
        stats.setTotalAdmins(adminRepository.countByActiveTrue());
        stats.setPendingTasks(getTasksCountByStatus("PENDING"));
        stats.setInProgressTasks(getTasksCountByStatus("IN_PROGRESS"));
        stats.setCompletedTasks(getTasksCountByStatus("COMPLETED"));
        return stats;
    }

    // Inner class for statistics
    public static class AdminStats {
        private long totalUsers;
        private long totalTasks;
        private long totalAdmins;
        private long pendingTasks;
        private long inProgressTasks;
        private long completedTasks;

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }

        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }

        public long getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }

        public long getInProgressTasks() { return inProgressTasks; }
        public void setInProgressTasks(long inProgressTasks) { this.inProgressTasks = inProgressTasks; }

        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    }
}
