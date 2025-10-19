package org.example.taskmanager.service;


import jakarta.persistence.EntityNotFoundException;
import org.example.taskmanager.Status;
import org.example.taskmanager.model.Task;
import org.example.taskmanager.model.TaskEntity;
import org.example.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
public class TaskService {


    //ДОРАБОТКИ:
    //getAllTasksOfOneUserAssignedUser нужна проверка которая уберет DONE задачи, который пользователь уже завершил

    //если в switchTaskToInProgress мы проверяем пользователя на 5 активных задач, то такая же проверяющая логика должна
    // быть и при создании, изменениии. иначе: "User ID: 1 already got 11 active tasks.One user may handle maximum 5 tasks with status IN_PROGRESS."


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
        return fromEntityToDomain(savedEntity);
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
            throw new IllegalStateException("Field Status should be empty because it's going to get Status.UPDATED by default case: "+ fromEntityToDomain(taskEntity));
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

    public Task switchTaskToInProgress(Long id) {

        var entity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("There is no task found by ID: "+id));

        if(entity.getAssignedUserId() == null || entity.getAssignedUserId() == 0){
            throw new IllegalStateException("The field 'assigned user' should be filled by the ID number of performer. " +
                    "Correct the field first and try again!");
        }
        int sumOfTasksPerOne = getAllEntityTasksOfOneUserByAssignedUserId(entity).size();
        if(sumOfTasksPerOne > 4){
            throw new IllegalStateException("User ID: " + entity.getAssignedUserId() + " already got " + sumOfTasksPerOne + " active tasks. Cannot switch to IN_PROGRESS if user have more than 4 active tasks.");
        } else{
            var entityToSwitch = new TaskEntity(
                    entity.getId(),
                    entity.getCreatorId(),
                    entity.getAssignedUserId(),
                    entity.setStatus(Status.IN_PROGRESS),
                    entity.getCreateDateTime(),
                    entity.getDeadLineDate(),
                    entity.getPriority()
            );
            var switched = taskRepository.save(entityToSwitch);
            return fromEntityToDomain(switched);
        }
    }


    private List<TaskEntity> getAllEntityTasksOfOneUserByAssignedUserId(TaskEntity entity){
        TaskEntity probe = new TaskEntity();
        probe.setAssignedUserId(entity.getAssignedUserId());
        Example<TaskEntity> example = Example.of(probe);
        return taskRepository.findAll(example);
    }



    public List<Task> getAllTasksOfOneUserAssignedUser(Long assignedUserId){
        TaskEntity probe = new TaskEntity();
        probe.setAssignedUserId(assignedUserId);
        List<Task> tasks = getAllEntityTasksOfOneUserByAssignedUserId(probe).stream()
                .map(this::fromEntityToDomain)
                .toList();
        if (tasks.isEmpty()){
            throw new NoSuchElementException("There is no tasks found for user with userID: " + assignedUserId);
        } else return tasks;
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

