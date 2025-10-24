package org.example.taskmanager.tasks;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        log.info("getTaskById method called with id = {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("getAllTasks method called");
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    @PostMapping
    public ResponseEntity<Task> createNewTask(@RequestBody @Valid Task taskToCreate) {
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
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("deleteTask method called: id={}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok()
                .build();
    }


    @PostMapping("/{id}/start")
    public ResponseEntity<String> switchTaskToInProgress(@PathVariable Long id) {
        log.info("switchTaskToInProgress method called with ID: " + id);
        taskService.switchTaskToInProgress(id);
        return ResponseEntity.ok()
                .body("Task id = " +id + " successfully switched to IN_PROGRESS");
    }


    @GetMapping("/user/{assignedUserId}")
    public ResponseEntity<List<Task>> getAllTasksOfOneUserAssignedUser(@PathVariable Long assignedUserId) {
        return ResponseEntity.ok(taskService.getAllTasksOfOneAssignedUser(assignedUserId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> getTaskDone(@PathVariable Long id) {
        log.info("getTaskDone method called with ID: " + id);
        return ResponseEntity.ok(taskService.getTaskDone(id));
    }


    @GetMapping("/filter")
    public ResponseEntity<List<Task>> searchAllByFilter(
            @RequestParam (name = "creatorId", required = false) Long creatorId,
            @RequestParam (name = "assignedUserId",required = false) Long assignedUserId,
            @RequestParam (name = "status", required = false) Status status,
            @RequestParam (name = "priority", required = false) Priority priority,
            @RequestParam (name = "pageSize", required = false) Integer pageSize,
            @RequestParam (name = "pageNum", required = false) Integer pageNum
    ) {
        log.info("searchAllByFilter method called");
        var filter = new TaskSearchFilter(creatorId, assignedUserId, status, priority, pageSize, pageNum);

        return ResponseEntity.ok(taskService.searchAllByFilter(filter));
    }


}

