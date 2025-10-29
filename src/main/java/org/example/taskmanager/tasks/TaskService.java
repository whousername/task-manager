package org.example.taskmanager.tasks;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class TaskService {


    private static final Logger logService = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public Task getTaskById(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by ID: "+id));

        return TaskMapper.fromEntityToDomain(taskEntity);
    }


    public Page<Task> getAllTasks(Pageable pageable){
        return taskRepository.findAll(pageable)
                .map(TaskMapper::fromEntityToDomain);
    }


    @Transactional
    public Task createNewTask(Task taskToCreate)
    {
        if (taskToCreate.deadLineDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Deadline cannot be in the past!");
        }

        var entityToSave =  TaskMapper.fromDomainToEntity(taskToCreate);
        entityToSave.setStatus(Status.CREATED);

        var savedEntity = taskRepository.save(entityToSave);
        logService.info("New task created with id = " + savedEntity.getId());
        return TaskMapper.fromEntityToDomain(savedEntity);
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

        if (taskEntity.getStatus() == Status.DONE){
            throw new IllegalStateException("Cannot modify tasks with Status.DONE, first switch it to IN_PROGRESS " + TaskMapper.fromEntityToDomain(taskEntity));
        }
        if(dataToUpdate.status() == Status.CREATED){
            throw new IllegalArgumentException("Cannot modify task-Status to Status.CREATED, id = "+id);
        }
        if (dataToUpdate.deadLineDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Dead-line date should be before the time of creation!");
        }

        var taskToUpdate = TaskMapper.fromDomainToEntity(dataToUpdate);
        taskToUpdate.setId(taskEntity.getId());
        taskToUpdate.setStatus(Status.UPDATED);


        var updatedTask = taskRepository.save(taskToUpdate);
        logService.info("Task id = "+updatedTask.getId() + " successfully updated.");
        return TaskMapper.fromEntityToDomain(updatedTask);
    }


    @Transactional
    public void switchTaskToInProgress(Long id) {
        var entity = taskRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("There is no task found by ID: "+id));

        int countEntities = taskRepository.countByAssignedUserIdAndStatus(entity.getAssignedUserId(),Status.IN_PROGRESS);
        if(countEntities > 4){
            throw new IllegalStateException("User ID " + entity.getAssignedUserId() + " already got " + countEntities + " active tasks. Cannot switch to IN_PROGRESS if user has more than 4 active tasks.");
        }
        taskRepository.setStatus(id, Status.IN_PROGRESS);
        logService.info("Task id = "+entity.getId()+" switched to Status.IN_PROGRESS.");
    }


    public List<Task> getAllTasksOfOneAssignedUser(Long assignedUserId){
        List<Task> tasks = taskRepository.findAllEntitiesByAssignedUserId(assignedUserId).stream()
                .map(TaskMapper::fromEntityToDomain)
                .toList();
        if (tasks.isEmpty()){
            throw new NoSuchElementException("There is no tasks found for user with userID: " + assignedUserId);
        } else return tasks;
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

            return TaskMapper.fromEntityToDomain(updatedEntity);
        }

    }

    public List<Task> searchAllByFilter(TaskSearchFilter filter) {

        Integer pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        Integer pageNumber = filter.pageNum() != null ? filter.pageNum() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

       List <TaskEntity> allEntities = taskRepository.searchAllByFilter(
                filter.creatorId(),
                filter.assignedUserId(),
                filter.status(),
                filter.priority(),
                pageable);
       if(allEntities.isEmpty()){
           throw new EntityNotFoundException("Not found task by filter: " + filter);
       }

       return allEntities.stream()
               .map(TaskMapper::fromEntityToDomain)
               .toList();
    }
}


