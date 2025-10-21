package org.example.taskmanager.tasks;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
            return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(){
        log.info("getAllTasks method called");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    public ResponseEntity<Task> createNewTask(@RequestBody @Valid Task taskToCreate){
        log.info("createNewTask method called");
            return ResponseEntity.status(201)
                    .body(taskService.createNewTask(taskToCreate));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Task> editTask(
            @PathVariable("id") Long id,
            @RequestBody @Valid Task taskToEdit
    ) {
        log.info("editTask method called: id={}, taskToEdit={}", id, taskToEdit);
            var updatedTask = taskService.editTask(id, taskToEdit);
            return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        log.info("deleteTask method called: id={}", id);
            taskService.deleteTask(id);
            return ResponseEntity.ok()
                    .build();
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<Task> switchTaskToInProgress(@PathVariable Long id){
        log.info("switchTaskToInProgress method called with ID: " + id);
            return ResponseEntity.ok(taskService.switchTaskToInProgress(id));
    }


    @GetMapping("/user/{assignedUserId}")
    public ResponseEntity<List<Task>> getAllTasksOfOneUserAssignedUser(@PathVariable Long assignedUserId){
            return ResponseEntity.ok(taskService.getAllTasksOfOneAssignedUser(assignedUserId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> getTaskDone(@PathVariable Long id){
        log.info("getTaskDone method called with ID: " + id);
        return ResponseEntity.ok(taskService.getTaskDone(id));
    }


}

