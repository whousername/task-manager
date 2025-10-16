package org.example.taskmanager.service;


import org.example.taskmanager.Priority;
import org.example.taskmanager.Status;
import org.example.taskmanager.model.Task;
import org.example.taskmanager.repository.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final Repository repository;


    public TaskService(Repository repository) {
        this.repository = repository;
    }

    public Task getTaskByIdService(Long id) throws NoSuchElementException {
        if(repository.getTaskById(id)==null){
            throw new NoSuchElementException("There is no elements found by ID: " + id);
        }
        return repository.getTaskById(id);
    }
    
    public Map<Long, Task> getAllTasks(){
        return repository.getRepository();
    }


    public Task createNewTask(Task taskToCreate) {
        if(taskToCreate.id() != null){
            throw new IllegalArgumentException("ID should be empty!");
        }
        var newTask = new Task(
                repository.getIdCounter().incrementAndGet(),
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                Status.CREATED,
                LocalDateTime.now(),
                taskToCreate.deadLineDate(),
                taskToCreate.priority()
        );
        repository.createNewTask(newTask.id(),newTask);
        return newTask;
    }

    public void deleteTask(Long id) {
        if(!repository.getRepository().containsKey(id)){
            throw new NoSuchElementException("There is no elements found by ID: " + id);
        } else repository.getRepository().remove(id);
    }


    public Task editTask(Long id, Task dataToUpdate) {
        if(repository.getTaskById(id) == null){
            throw new NoSuchElementException("There is no task found by ID: " + id);
        }
        var task = repository.getTaskById(id);
        if (task.status() == Status.DONE){
            throw new IllegalStateException("Cannot modify tasks with Status.DONE" + task);
        } else {
            var updatedTask = new Task(
                    task.id(),
                    dataToUpdate.creatorId(),
                    dataToUpdate.assignedUserId(),
                    Status.UPDATED,
                    LocalDateTime.now(),
                    dataToUpdate.deadLineDate(),
                    dataToUpdate.priority()
            );
            repository.getRepository().put(task.id(), updatedTask);
            return updatedTask;
        }
    }
}

