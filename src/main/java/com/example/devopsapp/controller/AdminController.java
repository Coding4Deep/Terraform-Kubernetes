package com.example.devopsapp.controller;

import com.example.devopsapp.model.Admin;
import com.example.devopsapp.model.User;
import com.example.devopsapp.model.Task;
import com.example.devopsapp.service.AdminService;
import com.example.devopsapp.service.SystemMonitoringService;
import com.example.devopsapp.service.ApplicationMonitoringService;
import com.example.devopsapp.service.UserService;
import com.example.devopsapp.service.TaskService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SystemMonitoringService systemMonitoringService;

    @Autowired
    private ApplicationMonitoringService applicationMonitoringService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    // Email and password validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Admin Registration
    @GetMapping("/register")
    public String adminRegisterPage(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/register";
    }

    @PostMapping("/register")
    public String registerAdmin(@ModelAttribute("admin") Admin admin, Model model) {
        
        // Enhanced validation
        if (admin.getUsername() == null || admin.getUsername().trim().length() < 3) {
            model.addAttribute("errorMsg", "Username must be at least 3 characters long");
            return "admin/register";
        }

        if (admin.getEmail() == null || !EMAIL_PATTERN.matcher(admin.getEmail()).matches()) {
            model.addAttribute("errorMsg", "Please enter a valid email address");
            return "admin/register";
        }

        if (admin.getPassword() == null || !PASSWORD_PATTERN.matcher(admin.getPassword()).matches()) {
            model.addAttribute("errorMsg", "Password must be at least 8 characters with uppercase, lowercase, and number");
            return "admin/register";
        }

        // Check for existing admin
        if (adminService.findByEmail(admin.getEmail()).isPresent()) {
            model.addAttribute("errorMsg", "Email already registered");
            return "admin/register";
        }

        if (adminService.findByUsername(admin.getUsername()).isPresent()) {
            model.addAttribute("errorMsg", "Username already taken");
            return "admin/register";
        }

        try {
            admin.setCreatedBy("SELF_REGISTRATION");
            adminService.saveAdmin(admin);
            model.addAttribute("msg", "Admin registration successful! Please login with your credentials.");
            return "admin/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Registration failed. Please try again.");
            return "admin/register";
        }
    }

    // Admin Login
    @GetMapping("/login")
    public String adminLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully.");
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String processAdminLogin() {
        // This will be handled by Spring Security
        return "redirect:/admin/dashboard";
    }

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        String email = principal.getName();
        Optional<Admin> adminOpt = adminService.findByEmail(email);

        if (adminOpt.isEmpty()) {
            return "redirect:/admin/login";
        }

        Admin admin = adminOpt.get();
        adminService.updateLastLogin(admin.getId());

        // Admin information
        model.addAttribute("adminName", admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
        model.addAttribute("adminEmail", admin.getEmail());

        // Statistics
        AdminService.AdminStats stats = adminService.getAdminStats();
        model.addAttribute("stats", stats);

        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());

        return "admin/users";
    }

    @PostMapping("/users/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> userData, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        try {
            User user = new User();
            user.setUsername(userData.get("username"));
            user.setEmail(userData.get("email"));
            user.setPassword(userData.get("password"));

            User savedUser = adminService.createUser(user);
            
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("userId", savedUser.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/users/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean deleted = adminService.deleteUser(userId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "User deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "User not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/users/{userId}/tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserTasks(@PathVariable String userId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<Task> tasks = adminService.getTasksByUserId(userId);
            Optional<User> user = adminService.getUserById(userId);
            
            response.put("success", true);
            response.put("tasks", tasks);
            response.put("user", user.orElse(null));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching user tasks: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // System Monitoring
    @GetMapping("/system-monitor")
    public String systemMonitor(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        return "admin/system-monitor";
    }

    @GetMapping("/api/system-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> systemInfo = systemMonitoringService.getSystemInfo();
        // Add enhanced container and memory information
        systemInfo.putAll(systemMonitoringService.getContainerInfo());
        systemInfo.putAll(systemMonitoringService.getDetailedMemoryInfo());
        systemInfo.putAll(systemMonitoringService.getNetworkPorts());
        return ResponseEntity.ok(systemInfo);
    }

    @GetMapping("/api/live-processes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLiveProcesses(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> processInfo = systemMonitoringService.getLiveProcessInfo();
        return ResponseEntity.ok(processInfo);
    }

    @GetMapping("/api/container-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContainerStats(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> containerInfo = systemMonitoringService.getContainerInfo();
        return ResponseEntity.ok(containerInfo);
    }

    // Detailed Task Views
    @GetMapping("/tasks/pending")
    public String viewPendingTasks(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<Task> pendingTasks = adminService.getAllTasks().stream()
                .filter(task -> "PENDING".equals(task.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        // Add user information for each task
        for (Task task : pendingTasks) {
            Optional<User> user = adminService.getUserById(task.getUserId());
            if (user.isPresent()) {
                task.setUserEmail(user.get().getEmail());
                task.setUserName(user.get().getUsername());
            }
        }
        
        model.addAttribute("tasks", pendingTasks);
        model.addAttribute("taskType", "Pending");
        model.addAttribute("totalTasks", pendingTasks.size());
        
        return "admin/task-details";
    }

    @GetMapping("/tasks/in-progress")
    public String viewInProgressTasks(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<Task> inProgressTasks = adminService.getAllTasks().stream()
                .filter(task -> "IN_PROGRESS".equals(task.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        // Add user information for each task
        for (Task task : inProgressTasks) {
            Optional<User> user = adminService.getUserById(task.getUserId());
            if (user.isPresent()) {
                task.setUserEmail(user.get().getEmail());
                task.setUserName(user.get().getUsername());
            }
        }
        
        model.addAttribute("tasks", inProgressTasks);
        model.addAttribute("taskType", "In Progress");
        model.addAttribute("totalTasks", inProgressTasks.size());
        
        return "admin/task-details";
    }

    @GetMapping("/tasks/completed")
    public String viewCompletedTasks(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<Task> completedTasks = adminService.getAllTasks().stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        // Add user information for each task
        for (Task task : completedTasks) {
            Optional<User> user = adminService.getUserById(task.getUserId());
            if (user.isPresent()) {
                task.setUserEmail(user.get().getEmail());
                task.setUserName(user.get().getUsername());
            }
        }
        
        model.addAttribute("tasks", completedTasks);
        model.addAttribute("taskType", "Completed");
        model.addAttribute("totalTasks", completedTasks.size());
        
        return "admin/task-details";
    }

    // Detailed User Views
    @GetMapping("/users/details")
    public String viewUserDetails(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<User> users = adminService.getAllUsers();
        
        // Add task count for each user
        for (User user : users) {
            List<Task> userTasks = adminService.getTasksByUserId(user.getId());
            user.setTotalTasks(userTasks.size());
            user.setPendingTasks((int) userTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
            user.setCompletedTasks((int) userTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        }
        
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        
        return "admin/user-details";
    }

    // User Task Management
    @GetMapping("/users/{userId}/tasks")
    public String viewUserTasks(@PathVariable String userId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        Optional<User> userOpt = adminService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users/details";
        }

        User user = userOpt.get();
        List<Task> userTasks = adminService.getTasksByUserId(userId);
        
        model.addAttribute("user", user);
        model.addAttribute("tasks", userTasks);
        model.addAttribute("totalTasks", userTasks.size());
        model.addAttribute("pendingTasks", userTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count());
        model.addAttribute("completedTasks", userTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        
        return "admin/user-tasks";
    }

    @GetMapping("/users/{userId}/edit")
    public String editUser(@PathVariable String userId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        Optional<User> userOpt = adminService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users/details";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        
        return "admin/edit-user";
    }

    @PostMapping("/users/{userId}/edit")
    public String updateUser(@PathVariable String userId, 
                           @RequestParam String username,
                           @RequestParam String email,
                           Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        try {
            Optional<User> userOpt = adminService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return "redirect:/admin/users/details";
            }

            User user = userOpt.get();
            user.setUsername(username);
            user.setEmail(email);
            
            adminService.updateUser(user);
            
            model.addAttribute("msg", "User updated successfully!");
            return "redirect:/admin/users/details";
            
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Failed to update user: " + e.getMessage());
            return "admin/edit-user";
        }
    }

    @GetMapping("/api/process-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProcessInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> processInfo = systemMonitoringService.getProcessInfo();
        return ResponseEntity.ok(processInfo);
    }

    @GetMapping("/api/docker-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDockerInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> dockerInfo = systemMonitoringService.getDockerInfo();
        return ResponseEntity.ok(dockerInfo);
    }

    // Application Monitoring
    @GetMapping("/app-monitor")
    public String applicationMonitor(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        return "admin/app-monitor";
    }

    @GetMapping("/api/app-health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApplicationHealth(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> appHealth = applicationMonitoringService.getApplicationHealth();
        return ResponseEntity.ok(appHealth);
    }

    @GetMapping("/api/service-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceStatus(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> serviceStatus = applicationMonitoringService.getDetailedServiceStatus();
        return ResponseEntity.ok(serviceStatus);
    }

    // Task Management
    @GetMapping("/tasks")
    public String manageTasks(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }

        List<Task> tasks = adminService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("totalTasks", tasks.size());

        return "admin/tasks";
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String taskId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean deleted = adminService.deleteTask(taskId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Task deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting task: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
