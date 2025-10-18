package org.example.taskmanager.service;


import jakarta.persistence.EntityNotFoundException;
import org.example.taskmanager.Status;
import org.example.taskmanager.model.Task;
import org.example.taskmanager.model.TaskEntity;
import org.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {



    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public Task getTaskById(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by ID: "+id));

        return fromEntityToDomain(taskEntity);
    }
    
    public List<Task> getAllTasks(){
        List<TaskEntity> allEntities = taskRepository.findAll();
        return allEntities.stream()
                .map(this::fromEntityToDomain)
                .toList();
    }


    public Task createNewTask(Task taskToCreate)
    {
        if (taskToCreate.id() != null){
            throw new IllegalArgumentException("ID should be empty!");
        }
        if (taskToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty!");
        }
        var entityToSave = new TaskEntity(
                null,
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                Status.CREATED,
                LocalDateTime.now(),
                taskToCreate.deadLineDate(),
                taskToCreate.priority()
        );
        var savedEntity = taskRepository.save(entityToSave);
        return fromEntityToDomain(savedEntity);  //мапим и возвращаем сущность с уже проставленным айди из базы
    }



    public void deleteTask(Long id)
    {
        TaskEntity entityToDelete = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException( "There is no task found by ID: "+id));
        taskRepository.delete(entityToDelete);
    }



    public Task editTask(Long id, Task dataToUpdate)
    {
        var taskEntity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException( "There is no task found by ID: "+id));

        if (taskEntity.getStatus() == Status.DONE){
            throw new IllegalStateException("Cannot modify tasks with Status.DONE: " + fromEntityToDomain(taskEntity));
        }
        if (dataToUpdate.status() != null){
            throw new IllegalArgumentException("Field Status should be empty because it's going to get Status.UPDATED by default case: "+ fromEntityToDomain(taskEntity));
        }
          else {
            var entityToUpdate = new TaskEntity(
                    taskEntity.getId(),
                    dataToUpdate.creatorId(),
                    dataToUpdate.assignedUserId(),
                    Status.UPDATED,
                    taskEntity.getCreateDateTime(),
                    dataToUpdate.deadLineDate(),
                    dataToUpdate.priority()
            );
            var updatedEntity = taskRepository.save(entityToUpdate);
            return fromEntityToDomain(updatedEntity);
          }
    }



    private Task fromEntityToDomain (TaskEntity entity){
        return new Task(
                entity.getId(),
                entity.getCreatorId(),
                entity.getAssignedUserId(),
                entity.getStatus(),
                entity.getCreateDateTime(),
                entity.getDeadLineDate(),
                entity.getPriority()
        );
    }
}

