package org.example.taskmanager.repository;

import org.example.taskmanager.Priority;
import org.example.taskmanager.Status;
import org.example.taskmanager.model.Task;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@org.springframework.stereotype.Repository
public class Repository {


    private final AtomicLong idCounter;

    private final Map<Long, Task> repository;

    public Repository(AtomicLong idCounter, Map<Long, Task> repository) {
        this.idCounter = new AtomicLong();
        this.repository = new HashMap<>(Map.of(
              1L,new Task(1L, 1L,1L, Status.CREATED, LocalDateTime.now(), LocalDateTime.now().plusDays(10), Priority.LOW),
              2L,new Task(2L, 1L,1L, Status.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now().plusDays(5), Priority.MEDIUM),
              3L,new Task(3L, 1L,1L, Status.IN_PROGRESS, LocalDateTime.now(), LocalDateTime.now().plusDays(3), Priority.LOW)
      ));
    }

    public AtomicLong getIdCounter() {
        return idCounter;
    }

    public Map<Long, Task> getRepository() {
        return repository;
    }

    public Task getTaskById(Long id){
        return repository.get(id);
    }

    public void createNewTask(Long id, Task task){
        repository.put(id,task);
    }
}
