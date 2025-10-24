package org.example.taskmanager.tasks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {


    List<TaskEntity> findAllEntitiesByAssignedUserId (Long assignedUserId);

    int countAllEntitiesInProgressByAssignedUserId (Long assignedUserId);


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



    @Query("""
            SELECT t from TaskEntity t
            WHERE (:creatorId IS NULL OR t.creatorId = :creatorId)
            AND (:assignedUserId IS NULL OR t.assignedUserId = :assignedUserId)
            AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            """)
    List<TaskEntity> searchAllByFilter (
            @Param("creatorId") Long creatorId,
            @Param ("assignedUserId") Long assignedUserId,
            @Param ("status") Status status,
            @Param ("priority") Priority priority,
            Pageable pageable);
}
