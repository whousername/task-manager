package org.example.taskmanager.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {


    List<TaskEntity> findAllEntitiesByAssignedUserId (Long assignedUserId);

    List<TaskEntity> findAllEntitiesInProgressByAssignedUserId (Long assignedUserId);


    @Modifying
    @Query("""
            update TaskEntity entity
            set entity.status = :status
            where entity.id = :id
            """
    )
    void setStatus(
            @Param("id") Long id,
            @Param("status") Status status);

}
