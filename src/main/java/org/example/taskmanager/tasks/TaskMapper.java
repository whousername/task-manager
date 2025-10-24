package org.example.taskmanager.tasks;


public class TaskMapper {

    public static Task fromEntityToDomain (TaskEntity entity){
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

    public static TaskEntity fromDomainToEntity (Task task){
        return new TaskEntity(
                task.id(),
                task.title(),
                task.description(),
                task.creatorId(),
                task.assignedUserId(),
                task.status(),
                task.createDateTime(),
                task.deadLineDate(),
                task.priority(),
                task.doneDataTime()
        );
    }


}
