package org.example.taskmanager.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.taskmanager.model.Task;
import org.example.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping ("/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id){
        log.info("getTaskById method called with id = {}", id);
        try {
            return ResponseEntity.ok(taskService.getTaskById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404)
                    .build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        log.info("getAllTasks method called");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    public ResponseEntity<Task> createNewTask(@RequestBody Task taskToCreate){
        log.info("createNewTask method called");
        try {
            return ResponseEntity.status(201)
                    .body(taskService.createNewTask(taskToCreate));
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Task> editTask(
            @PathVariable("id") Long id,
            @RequestBody Task taskToEdit
    ) {
        log.info("editTask method called: id={}, taskToEdit={}", id, taskToEdit);
        try {
            var updatedTask = taskService.editTask(id, taskToEdit);
            return ResponseEntity.ok(updatedTask);
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(404)
                    .build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        log.info("deleteTask method called: id={}", id);
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok()
                    .build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404)
                    .build();
        }
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<Task> switchTaskToInProgress(@PathVariable Long id){
        log.info("switchTaskToInProgress method called with ID: " + id);
        try {
            var switchedTask = taskService.switchTaskToInProgress(id);
            return ResponseEntity.ok(switchedTask);
        } catch (NoSuchElementException | IllegalStateException e) {
            return ResponseEntity.status(404)
                    .build();
        }
    }


    @GetMapping("/user/{assignedUserId}")
    public ResponseEntity<List<Task>> getAllTasksOfOneUserAssignedUser(@PathVariable Long assignedUserId){
        try {
            return ResponseEntity.ok(taskService.getAllTasksOfOneUserAssignedUser(assignedUserId));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404)
                    .build();
        }
    }


}

