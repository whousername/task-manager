package org.example.taskmanager.model;

import jakarta.persistence.*;
import org.example.taskmanager.Priority;
import org.example.taskmanager.Status;

import java.time.LocalDateTime;

@Table(name = "tasks")
@Entity
public class TaskEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) //чето там генератор какой то?
    private Long id;

    @Column(name = "creator id")
    private Long creatorId;

    @Column(name = "assigned user id")
    private Long assignedUserId;

    @Enumerated(EnumType.STRING) //аннотация для сохранения енама в базу как стринг
    @Column(name = "status")
    private Status status;

    @Column(name = "create date and time")
    private LocalDateTime createDateTime;

    @Column(name = "dead line")
    private LocalDateTime deadLineDate;

    @Enumerated(EnumType.STRING) //аннотация для сохранения енама в базу как стринг
    @Column(name = "priority")
    private Priority priority;



    public TaskEntity() {
    }

    public TaskEntity(Long id, Long creatorId, Long assignedUserId,
                      Status status, LocalDateTime createDateTime,
                      LocalDateTime deadLineDate, Priority priority
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.createDateTime = createDateTime;
        this.deadLineDate = deadLineDate;
        this.priority = priority;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getDeadLineDate() {
        return deadLineDate;
    }

    public void setDeadLineDate(LocalDateTime deadLineDate) {
        this.deadLineDate = deadLineDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
