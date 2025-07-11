package com.example.devopsapp.controller;

import com.example.devopsapp.model.Task;
import com.example.devopsapp.model.User;
import com.example.devopsapp.service.TaskService;
import com.example.devopsapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Map<String, String> taskData, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            Task task = new Task();
            task.setTitle(taskData.get("title"));
            task.setDescription(taskData.get("description"));
            task.setPriority(taskData.getOrDefault("priority", "MEDIUM"));
            task.setUserId(user.getId());
            
            // Parse due date if provided
            String dueDateStr = taskData.get("dueDate");
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                try {
                    LocalDateTime dueDate = LocalDateTime.parse(dueDateStr + "T23:59:59");
                    task.setDueDate(dueDate);
                } catch (Exception e) {
                    // If parsing fails, ignore due date
                }
            }

            Task savedTask = taskService.createTask(task);
            
            response.put("success", true);
            response.put("message", "Task created successfully");
            response.put("task", convertTaskToMap(savedTask));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating task: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTasks(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            List<Task> tasks = taskService.getTasksByUserId(user.getId());
            
            response.put("success", true);
            response.put("tasks", tasks.stream().map(this::convertTaskToMap).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching tasks: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{taskId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaskStatus(@PathVariable String taskId, 
                                                               @RequestBody Map<String, String> statusData, 
                                                               Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            String newStatus = statusData.get("status");
            
            Task updatedTask = taskService.updateTaskStatus(taskId, newStatus, user.getId());
            
            if (updatedTask != null) {
                response.put("success", true);
                response.put("message", "Task status updated successfully");
                response.put("task", convertTaskToMap(updatedTask));
            } else {
                response.put("success", false);
                response.put("message", "Task not found or access denied");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating task: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable String taskId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            boolean deleted = taskService.deleteTask(taskId, user.getId());
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Task deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Task not found or access denied");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting task: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("title", task.getTitle());
        taskMap.put("description", task.getDescription());
        taskMap.put("status", task.getStatus());
        taskMap.put("priority", task.getPriority());
        taskMap.put("createdAt", task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        taskMap.put("updatedAt", task.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        if (task.getDueDate() != null) {
            taskMap.put("dueDate", task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        
        return taskMap;
    }
}
