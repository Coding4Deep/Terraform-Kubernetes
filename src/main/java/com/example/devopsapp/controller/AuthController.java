package com.example.devopsapp.controller;

import com.example.devopsapp.model.User;
import com.example.devopsapp.service.UserService;
import com.example.devopsapp.service.TaskService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    // Password validation pattern (at least 8 chars, 1 uppercase, 1 lowercase, 1 digit)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // Enhanced validation
        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            model.addAttribute("errorMsg", "Username must be at least 3 characters long");
            return "register";
        }

        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            model.addAttribute("errorMsg", "Please enter a valid email address");
            return "register";
        }

        if (user.getPassword() == null || !PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            model.addAttribute("errorMsg", "Password must be at least 8 characters with uppercase, lowercase, and number");
            return "register";
        }

        // Check for existing users
        Optional<User> byEmail = userService.findByEmail(user.getEmail());
        Optional<User> byUsername = userService.findByUsername(user.getUsername());

        if (byEmail.isPresent()) {
            model.addAttribute("errorMsg", "Email already registered");
            return "register";
        }

        if (byUsername.isPresent()) {
            model.addAttribute("errorMsg", "Username already taken");
            return "register";
        }

        try {
            userService.saveUser(user);
            model.addAttribute("msg", "Registration successful! Please login with your credentials.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Registration failed. Please try again.");
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();

        // User information
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("userId", user.getId());

        // Learning materials
        model.addAttribute("materials", new String[]{
                "Docker Basics",
                "Kubernetes Tutorial", 
                "Jenkins CI/CD",
                "Terraform Infrastructure as Code",
                "RabbitMQ Messaging",
                "Memcached Caching",
                "MongoDB Database Management",
                "Spring Boot Development",
                "AWS Cloud Services",
                "DevOps Best Practices"
        });

        // User statistics
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("totalUsers", allUsers.size());

        // Task statistics
        long totalTasks = taskService.getTaskCountByUserId(user.getId());
        long pendingTasks = taskService.getTaskCountByUserIdAndStatus(user.getId(), "PENDING");
        long inProgressTasks = taskService.getTaskCountByUserIdAndStatus(user.getId(), "IN_PROGRESS");
        long completedTasks = taskService.getTaskCountByUserIdAndStatus(user.getId(), "COMPLETED");

        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("completedTasks", completedTasks);

        // Session information for security
        model.addAttribute("sessionId", session.getId());
        model.addAttribute("lastAccessTime", session.getLastAccessedTime());

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        
        return "profile";
    }
}
