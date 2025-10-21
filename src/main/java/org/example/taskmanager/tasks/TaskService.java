package org.example.taskmanager.tasks;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class TaskService {


    //ДОРАБОТКИ:

    //switchTaskToInProgress возвращает результат до свитча

    //раскидать правильно по пакетам

    //запушить в гитхаб

    //обновить базу данных


    private static final Logger logService = LoggerFactory.getLogger(TaskService.class);


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


    @Transactional
    public Task createNewTask(Task taskToCreate)
    {
        if (taskToCreate.deadLineDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Deadline cannot be in the past!");
        }
        var entityToSave = new TaskEntity(
                null,
                taskToCreate.title(),
                taskToCreate.description(),
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                Status.CREATED,
                LocalDateTime.now(),
                taskToCreate.deadLineDate(),
                taskToCreate.priority(),
                taskToCreate.doneDataTime()
        );
        var savedEntity = taskRepository.save(entityToSave);
        logService.info("New task created with id = " + savedEntity.getId());

        return fromEntityToDomain(savedEntity);
    }


    public void deleteTask(Long id)
    {
        TaskEntity entityToDelete = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException( "There is no task found by ID: "+id));
        taskRepository.delete(entityToDelete);
        logService.info("Task id = "+id+" successfully deleted.");
    }


    @Transactional
    public Task editTask(Long id, Task dataToUpdate)
    {
        var taskEntity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException( "There is no task found by ID: "+id));

        if (taskEntity.getStatus() == Status.DONE && dataToUpdate.status() != Status.IN_PROGRESS){
            throw new IllegalStateException("Cannot modify tasks with Status.DONE, except reopening to IN_PROGRESS: " + fromEntityToDomain(taskEntity));
        }
        if(dataToUpdate.status() == Status.CREATED){
            throw new IllegalArgumentException("Cannot modify task-Status to Status.CREATED, id = "+id);
        }
        if (dataToUpdate.deadLineDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Dead-line date should be before the time of creation!");
        }

        var taskToUpdate = new TaskEntity(
                taskEntity.getId(),
                dataToUpdate.title(),
                dataToUpdate.description(),
                dataToUpdate.creatorId(),
                dataToUpdate.assignedUserId(),
                Status.UPDATED,
                taskEntity.getCreateDateTime(),
                dataToUpdate.deadLineDate(),
                dataToUpdate.priority(),
                dataToUpdate.doneDataTime()
        );

        var updatedTask = taskRepository.save(taskToUpdate);
        logService.info("Task id = "+updatedTask.getId() + " successfully updated.");

        return fromEntityToDomain(updatedTask);
    }


    @Transactional
    public Task switchTaskToInProgress(Long id) {
        var entity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("There is no task found by ID: "+id));
//        //возможно эта проверка больше не нужна потому что при создании жестко ограничиваю null
        if(entity.getAssignedUserId() == null || entity.getAssignedUserId() == 0) {
            throw new IllegalArgumentException("The field 'assigned user' should be filled by the ID number of performer. " +
                    "Correct the field first and try again!");
        }
        if(taskRepository.findAllEntitiesInProgressByAssignedUserId(entity.getAssignedUserId()).size() > 4){
            throw new IllegalStateException("User ID " + entity.getAssignedUserId() + " already got " + taskRepository.findAllEntitiesInProgressByAssignedUserId(entity.getAssignedUserId()).size() + " active tasks. Cannot switch to IN_PROGRESS if user has more than 4 active tasks.");
        } else{
            taskRepository.setStatus(id, Status.IN_PROGRESS);
            logService.info("Task id = "+entity.getId()+" switched to Status.IN_PROGRESS.");

            return fromEntityToDomain(entity);
            //или SELECT после UPDATE
        }
    }


    public List<Task> getAllTasksOfOneAssignedUser(Long assignedUserId){
        List<Task> tasks = taskRepository.findAllEntitiesByAssignedUserId(assignedUserId).stream()
                .map(this::fromEntityToDomain)
                .toList();
        if (tasks.isEmpty()){
            throw new NoSuchElementException("There is no tasks found for user with userID: " + assignedUserId);
        } else return tasks;
    }


    private Task fromEntityToDomain (TaskEntity entity){
        return new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatorId(),
                entity.getAssignedUserId(),
                entity.getStatus(),
                entity.getCreateDateTime(),
                entity.getDeadLineDate(),
                entity.getPriority(),
                entity.getDoneDataTime()
        );
    }


    public Task getTaskDone(Long id) {

        var entity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("There is no task found by ID: "+id));
        if(
                entity.getCreatorId() == null || entity.getCreatorId() == 0 ||
                entity.getAssignedUserId() == null || entity.getAssignedUserId() == 0 ||
                entity.getDeadLineDate() == null) {

            throw new IllegalArgumentException("Required fields: CreatorId, AssignedUserId, DeadLineDate. " +
                    "Correct the fields first and try again!");
        }
        else {
            var entityToUpdate = new TaskEntity(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getCreatorId(),
                    entity.getAssignedUserId(),
                    entity.setStatus(Status.DONE),
                    entity.getCreateDateTime(),
                    entity.getDeadLineDate(),
                    entity.getPriority(),
                    LocalDateTime.now()
            );
            var updatedEntity = taskRepository.save(entityToUpdate);
            logService.info("Task id = "+updatedEntity.getId()+" switched to Status.DONE.");

            return fromEntityToDomain(updatedEntity);
        }

    }

}

