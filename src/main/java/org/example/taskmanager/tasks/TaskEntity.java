package org.example.taskmanager.tasks;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "tasks")
@Entity
public class TaskEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) //чето там генератор какой то?
    private Long id;

    @Column(name = "task title", nullable = false)
    String title;

    @Column(name = "task description")
    String description;

    @Column(name = "creator id", nullable = false)
    private Long creatorId;

    @Column(name = "assigned user id", nullable = false)
    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "create date and time", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "dead line date and time", nullable = false)
    private LocalDateTime deadLineDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Column(name = "done date and time")
    LocalDateTime doneDataTime;



    public TaskEntity() {
    }

    public TaskEntity(Long id, String title, String description, Long creatorId, Long assignedUserId,
                      Status status, LocalDateTime createDateTime,
                      LocalDateTime deadLineDate, Priority priority,
                      LocalDateTime doneDataTime
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.createDateTime = createDateTime;
        this.deadLineDate = deadLineDate;
        this.priority = priority;
        this.doneDataTime = doneDataTime;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Status setStatus(Status status) {
        return this.status = status;
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

    public LocalDateTime getDoneDataTime() {
        return doneDataTime;
    }

    public void setDoneDataTime(LocalDateTime doneDataTime) {
        this.doneDataTime = doneDataTime;
    }
}
