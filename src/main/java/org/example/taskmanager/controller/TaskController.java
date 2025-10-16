package org.example.taskmanager.controller;

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
            return ResponseEntity.ok(taskService.getTaskByIdService(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .build();
        }
    }

    @GetMapping
    public ResponseEntity<Map<Long, Task>> getAllTasks(){
        log.info("getAllTasks method called");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PostMapping
    public ResponseEntity<Task> createNewTask(@RequestBody Task taskToCreate){
        log.info("createNewTask method called");
        return ResponseEntity.status(201)
                .body(taskService.createNewTask(taskToCreate));
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
        } catch (NoSuchElementException e) {
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
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .build();
        }
    }

}


//добавить title & description



//Вместо try/catch в каждом контроллере, можно централизовать обработку:
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<Void> handleNotFound(NoSuchElementException e) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//    }
//}
