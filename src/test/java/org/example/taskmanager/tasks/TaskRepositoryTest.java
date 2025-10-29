package org.example.taskmanager.tasks;

import jakarta.persistence.EntityNotFoundException;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TaskRepositoryTest {


    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    public TaskRepositoryTest(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    @Test
    void findAllEntitiesByAssignedUserIdTest_ReturnsAllEntitiesForAssignedUserId() {
        TaskEntity task1 = new TaskEntity(null,"TestTask1", "some1",1L,1L,Status.CREATED, LocalDateTime.now(),LocalDateTime.now().plusDays(5), Priority.LOW,null);
        TaskEntity task2 = new TaskEntity(null,"TestTask2", "some2",2L,1L,Status.CREATED, LocalDateTime.now(),LocalDateTime.now().plusDays(5), Priority.LOW,null);
        taskRepository.save(task1);
        taskRepository.save(task2);

        List<TaskEntity> entities = taskRepository.findAllEntitiesByAssignedUserId(1L);

        Assertions.assertThat(entities.size()).isEqualTo(2);
        Assertions.assertThat(entities)
                .extracting(TaskEntity::getAssignedUserId)
                .containsOnly(1L);
    }

    @Test
    void givenTasksWithDifferentStatuses_whenCountInProgressByAssignedUserId_thenReturnsOnlyInProgressCount() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);

        TaskEntity task1 = new TaskEntity(null,"TestTask1", "some1",1L,1L,
                Status.IN_PROGRESS, now, now.plusDays(5), Priority.LOW,null);
        TaskEntity task2 = new TaskEntity(null,"TestTask2", "some2",2L,1L,
                Status.CREATED, now, now.plusDays(5), Priority.LOW,null);

        taskRepository.saveAll(List.of(task1, task2));

        int countInProgress = taskRepository.countByAssignedUserIdAndStatus(1L,Status.IN_PROGRESS);
        int countCreated = taskRepository.countByAssignedUserIdAndStatus(1L,Status.CREATED);


        Assertions.assertThat(countInProgress).isEqualTo(1);
        Assertions.assertThat(countCreated).isEqualTo(1);
    }

    @Test
    void setStatusTest_ReturnsEntityWithUpdatedStatus() {
        TaskEntity task1 = new TaskEntity(null,"TestTask1", "some1",1L,1L,Status.CREATED, LocalDateTime.now(),LocalDateTime.now().plusDays(5), Priority.LOW,null);
        taskRepository.save(task1);

        taskRepository.setStatus(task1.getId(),Status.IN_PROGRESS);

        TaskEntity updatedEntity = taskRepository.findById(task1.getId())
                .orElseThrow(() -> new EntityNotFoundException ("Not found entity"));

        Assertions.assertThat(updatedEntity.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void searchAllByFilter_ReturnsListOfEntitiesFoundByFilter() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);

        TaskEntity task1 = new TaskEntity(null,"TestTask1", "some1",1L,1L,Status.CREATED, now,now.plusDays(5), Priority.LOW,null);
        TaskEntity task2 = new TaskEntity(null,"TestTask2", "some2",2L,2L,Status.UPDATED, now,now.plusDays(5), Priority.MEDIUM,null);
        TaskEntity task3 = new TaskEntity(null,"TestTask3", "some3",1L,1L,Status.IN_PROGRESS, now,now.plusDays(5), Priority.HIGH,null);
        TaskEntity task4 = new TaskEntity(null,"TestTask4", "some4",2L,2L,Status.UPDATED, now,now.plusDays(5), Priority.MEDIUM,null);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);

        Pageable unpaged = Pageable.unpaged();

        List<TaskEntity> foundEntities = taskRepository.searchAllByFilter(2L,2L,Status.UPDATED,Priority.MEDIUM, unpaged);

        Assertions.assertThat(foundEntities)
                .hasSize(2)
                .extracting(TaskEntity::getTitle)
                .containsExactlyInAnyOrder("TestTask2", "TestTask4");

        Assertions.assertThat(foundEntities)
                .allMatch(e -> e.getStatus() == Status.UPDATED && e.getPriority() == Priority.MEDIUM);
    }
}


//докатить тесты репо
//проверка с гпт
//видос
//комитить позже тесты репо