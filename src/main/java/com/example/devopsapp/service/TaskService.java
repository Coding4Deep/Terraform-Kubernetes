package com.example.devopsapp.service;

import com.example.devopsapp.model.Task;
import com.example.devopsapp.repository.TaskRepository;
import com.example.devopsapp.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TaskService(TaskRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        
        // Send notification to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_QUEUE,
                "New task created: " + savedTask.getTitle() + " by user: " + savedTask.getUserId());
        
        return savedTask;
    }

    public List<Task> getTasksByUserId(String userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Task> getTasksByUserIdAndStatus(String userId, String status) {
        return taskRepository.findByUserIdAndStatus(userId, status);
    }

    public Optional<Task> getTaskById(String taskId) {
        return taskRepository.findById(taskId);
    }

    public Task updateTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        
        // Send notification to RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_QUEUE,
                "Task updated: " + updatedTask.getTitle() + " - Status: " + updatedTask.getStatus());
        
        return updatedTask;
    }

    public boolean deleteTask(String taskId, String userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent() && taskOpt.get().getUserId().equals(userId)) {
            taskRepository.deleteById(taskId);
            
            // Send notification to RabbitMQ
            rabbitTemplate.convertAndSend(RabbitMQConfig.USER_QUEUE,
                    "Task deleted: " + taskOpt.get().getTitle() + " by user: " + userId);
            
            return true;
        }
        return false;
    }

    public long getTaskCountByUserId(String userId) {
        return taskRepository.countByUserId(userId);
    }

    public long getTaskCountByUserIdAndStatus(String userId, String status) {
        return taskRepository.countByUserIdAndStatus(userId, status);
    }

    public Task updateTaskStatus(String taskId, String status, String userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent() && taskOpt.get().getUserId().equals(userId)) {
            Task task = taskOpt.get();
            task.setStatus(status);
            return updateTask(task);
        }
        return null;
    }
}
