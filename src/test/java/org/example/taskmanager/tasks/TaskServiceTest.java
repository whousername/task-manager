package org.example.taskmanager.tasks;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;


    @InjectMocks
    private TaskService taskService;  //  Эта аннотация создаёт реальный объект TaskService
                                    //  и впрыскивает в него все моки, помеченные аннотацией @Mock.


    @Test
    void taskService_getTaskById_shouldCheckAvailabilityAndReturnTask() {
        Long id = 1L;

        var task1 = Task.builder()
                .id(id)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(1L)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        TaskEntity mappedEntity = TaskMapper.fromDomainToEntity(task1);

        when(taskRepository.findById(id)).thenReturn(Optional.of(mappedEntity));

        Task returnedTask = taskService.getTaskById(id);

        Assertions.assertThat(returnedTask)
                .isNotNull()
                .matches(t -> t.title().equals("test1"));

        verify(taskRepository).findById(id);
    }

    @Test
    void taskService_getTaskById_shouldCheckAvailabilityAndThrowException() {
        Long id = 1L;
        when(taskRepository.findById(id))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Not found task by ID: " +id);
        verify(taskRepository).findById(id);
    }



    @Test
    void taskService_getAllTasks_shouldMapAllToDomainAndReturnPage() {

        Pageable unpaged = Pageable.unpaged();

        var mockTask1 = Task.builder()
                .id(1L)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(1L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();
        var mockTask2 = Task.builder()
                .id(2L)
                .title("test2")
                .creatorId(1L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.HIGH)
                .build();

        var mappedMockTask1 = TaskMapper.fromDomainToEntity(mockTask1);
        var mappedMockTask2 = TaskMapper.fromDomainToEntity(mockTask2);


        when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mappedMockTask1,mappedMockTask2)));

        Page<Task> found = taskService.getAllTasks(unpaged);

        Assertions.assertThat(found)
                .isNotEmpty()
                .hasSize(2);

        Assertions.assertThat(found.getContent())
                        .extracting(Task::title)
                                .containsExactlyInAnyOrder("test1", "test2");

        verify(taskRepository).findAll(any(Pageable.class));

    }




    @Test
    void taskService_createNewTaskTest_shouldSetCreatedStatusAndReturnMappedTask() {
        var task1 = Task.builder()
                .title("test1")
                .creatorId(1L)
                .assignedUserId(1L)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task savedEntity = taskService.createNewTask(task1);

        Assertions.assertThat(savedEntity)
                .isNotNull()
                .matches(e -> e.status() == Status.CREATED)
                .matches(e -> e.createDateTime() != null);

        verify(taskRepository).save(any(TaskEntity.class));
    }




    @Test
    void taskService_deleteTask_shouldCallRepository() {
        Long id = 1L;
        var entityTask1 = new TaskEntity();

        when(taskRepository.findById(id)).thenReturn(Optional.of(entityTask1));

        taskService.deleteTask(id);

        verify(taskRepository).findById(any(Long.class));
        verify(taskRepository).delete(entityTask1);
    }

    @Test
    void taskService_deleteTask_shouldCallRepositoryAndThrowEntityNotFoundException() {

        Long id = 1L;
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("There is no task found by ID: "+id);

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).delete(any());
    }



    @Test
    void taskService_editTask_shouldReturnUpdatedTask() {
        Long id = 1L;
        var dataToUpdate = Task.builder()
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.IN_PROGRESS)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.MEDIUM)
                .build();

        var taskInRepo = Task.builder()
                .id(1L)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(1L)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        var taskEntity1 = TaskMapper.fromDomainToEntity(taskInRepo);
        when(taskRepository.findById(id)).thenReturn(Optional.of(taskEntity1));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var updatedTask = taskService.editTask(id,dataToUpdate);

        Assertions.assertThat(updatedTask)
                .isNotNull()
                .matches(t -> t.id().equals(1L) && t.title().equals("updTitle") && t.description().equals("updDesc"))
                .matches(t -> t.status() == Status.UPDATED);

        verify(taskRepository).findById(id);

        var captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(Status.UPDATED);
    }

    @Test
    void taskService_editTask_shouldThrowEntityNotFoundException() {
        Long id = 1L;
        var dataToUpdate = Task.builder()
                .build();

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.editTask(id,dataToUpdate))
                        .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("There is no task found");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).save(any());
    }

    @Test
    void taskService_editTask_shouldThrow_WhenStatusIsDone() {
        Long id = 1L;
        var dataToUpdate = Task.builder()
                .build();

        var taskInRepo = Task.builder()
                .id(1L)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(1L)
                .status(Status.DONE)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        var taskEntityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);
        when(taskRepository.findById(id)).thenReturn(Optional.of(taskEntityInRepo));

        Assertions.assertThatThrownBy(() -> taskService.editTask(id,dataToUpdate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify tasks with Status.DONE");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).save(any());
    }

    @Test
    void taskService_editTask_shouldThrow_WhenStatusIsCREATED() {
        Long id = 1L;
        var dataToUpdate = Task.builder()
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.MEDIUM)
                .build();

        var taskInRepo = Task.builder()
                .id(1L)
                .build();

        var taskEntityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);
        when(taskRepository.findById(id)).thenReturn(Optional.of(taskEntityInRepo));

        Assertions.assertThatThrownBy(() -> taskService.editTask(id,dataToUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot modify task-Status to Status.CREATED");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).save(any());
    }

    @Test
    void taskService_editTask_shouldThrow_WhenDateIsBeforeNow() {
        Long id = 1L;
        var dataToUpdate = Task.builder()
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.MEDIUM)
                .build();

        var taskInRepo = Task.builder()
                .id(1L)
                .build();

        var taskEntityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);
        when(taskRepository.findById(id)).thenReturn(Optional.of(taskEntityInRepo));

        Assertions.assertThatThrownBy(() -> taskService.editTask(id,dataToUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dead-line date should be before");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).save(any());
    }


    @Test
    void taskService_switchTaskToInProgress_changeStatusSuccess(){
        Long id = 1L;
        var taskInRepo = Task.builder()
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.MEDIUM)
                .build();
        var entityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);

        when(taskRepository.findById(id)).thenReturn(Optional.of(entityInRepo));
        when(taskRepository.countByAssignedUserIdAndStatus(entityInRepo.getAssignedUserId(),Status.IN_PROGRESS)).thenReturn(1);
        doNothing().when(taskRepository).setStatus(id, Status.IN_PROGRESS);

        taskService.switchTaskToInProgress(id);

        verify(taskRepository).findById(id);
        verify(taskRepository).countByAssignedUserIdAndStatus(entityInRepo.getAssignedUserId(), Status.IN_PROGRESS);
        verify(taskRepository).setStatus(id, Status.IN_PROGRESS);
    }


    @Test
    void taskService_switchTaskToInProgress_throwEntityNotFoundException() {
        Long id = 1L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.switchTaskToInProgress(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("There is no task found");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).countByAssignedUserIdAndStatus(1L,Status.IN_PROGRESS);
        verify(taskRepository,never()).save(any());
    }

    @Test
    void taskService_switchTaskToInProgress_throwIllegalStateException(){
        Long id = 1L;

        var taskInRepo = Task.builder()
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.MEDIUM)
                .build();
        var entityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);

        when(taskRepository.findById(id)).thenReturn(Optional.of(entityInRepo));
        when(taskRepository.countByAssignedUserIdAndStatus(entityInRepo.getAssignedUserId(),Status.IN_PROGRESS)).thenReturn(5);

        Assertions.assertThatThrownBy(()-> taskService.switchTaskToInProgress(id))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("User ID");

        verify(taskRepository).findById(id);
        verify(taskRepository).countByAssignedUserIdAndStatus(entityInRepo.getAssignedUserId(), Status.IN_PROGRESS);
        verify(taskRepository, never()).setStatus(id, Status.IN_PROGRESS);
    }

    @Test
    void taskService_getAllTasksOfOneAssignedUser_shouldReturnListOfTasks() {
        Long assignedUserId = 1L;
        var taskInRepo1 = Task.builder()
                .id(1L)
                .assignedUserId(1L)
                .build();
        var taskInRepo2 = Task.builder()
                .id(2L)
                .assignedUserId(1L)
                .build();
        var entityInRepo1 = TaskMapper.fromDomainToEntity(taskInRepo1);
        var entityInRepo2 = TaskMapper.fromDomainToEntity(taskInRepo2);

        when(taskRepository.findAllEntitiesByAssignedUserId(1L)).thenReturn(List.of(entityInRepo1, entityInRepo2));

        List<Task> entities = taskService.getAllTasksOfOneAssignedUser(assignedUserId);

        Assertions.assertThat(entities)
                .isNotNull()
                .allMatch(t -> t.assignedUserId().equals(1L))
                .hasSize(2);
        verify(taskRepository).findAllEntitiesByAssignedUserId(assignedUserId);
    }


    @Test
    void taskService_getTaskDone_shouldReturnUpdatedTaskStatusDONE() {
        Long id = 1L;
        var taskInRepo = Task.builder()
                .id(1L)
                .title("updTitle")
                .description("updDesc")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.IN_PROGRESS)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.MEDIUM)
                .build();
        var entityInRepo = TaskMapper.fromDomainToEntity(taskInRepo);

        when(taskRepository.findById(id)).thenReturn(Optional.of(entityInRepo));
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = taskService.getTaskDone(id);

        var captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        var savedEntity = captor.getValue();

        Assertions.assertThat(savedEntity.getDoneDataTime()).isNotNull();
        Assertions.assertThat(savedEntity.getStatus()).isEqualTo(Status.DONE);

        verify(taskRepository).findById(id);
    }


    @Test
    void taskService_getTaskDone_shouldThrowEntityNotFoundException() {
        Long id = 1L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> taskService.getTaskDone(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("There is no task found");

        verify(taskRepository).findById(id);
        verify(taskRepository,never()).save(any());
    }


    @Test
    void taskService_searchAllByFilter_ShouldReturnListOfTasks() {
        TaskSearchFilter filter = new TaskSearchFilter(
                2L,2L, Status.IN_PROGRESS, Priority.LOW, 10, 0);

        var taskInRepo1 = Task.builder()
                .id(1L)
                .title("updTitle1")
                .description("updDesc1")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.IN_PROGRESS)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.LOW)
                .build();
        var taskInRepo2 = Task.builder()
                .id(2L)
                .title("updTitle2")
                .description("updDesc2")
                .creatorId(2L)
                .assignedUserId(2L)
                .status(Status.IN_PROGRESS)
                .deadLineDate(LocalDateTime.now().minusDays(5))
                .priority(Priority.LOW)
                .build();
        var entityInRepo1 = TaskMapper.fromDomainToEntity(taskInRepo1);
        var entityInRepo2 = TaskMapper.fromDomainToEntity(taskInRepo2);

        when(taskRepository.searchAllByFilter(
                anyLong(),anyLong(), any(Status.class), any(Priority.class), any(Pageable.class)))
                .thenReturn(List.of(entityInRepo1, entityInRepo2));

        List<Task> result = taskService.searchAllByFilter(filter);



        Assertions.assertThat(result).isNotNull()
                        .hasSize(2);

        Assertions.assertThat(result.get(0).id()).isEqualTo(taskInRepo1.id());
        Assertions.assertThat(result.get(1).id()).isEqualTo(taskInRepo2.id());

        verify(taskRepository).searchAllByFilter(
                eq(filter.creatorId()),
                eq(filter.assignedUserId()),
                eq(filter.status()),
                eq(filter.priority()),
                any(Pageable.class)
        );
    }

    @Test
    void taskService_searchAllByFilter_ShouldThrowEntityNotFoundException() {

        TaskSearchFilter filter = new TaskSearchFilter(
                2L,2L, Status.IN_PROGRESS, Priority.LOW, 10, 0);

        when(taskRepository.searchAllByFilter(
                anyLong(),anyLong(), any(Status.class), any(Priority.class), any(Pageable.class)))
                .thenReturn(List.of());

        Assertions.assertThatThrownBy(() -> taskService.searchAllByFilter(filter))
                        .isInstanceOf(EntityNotFoundException.class)
                                .hasMessageContaining("Not found task");

        verify(taskRepository).searchAllByFilter(
                eq(filter.creatorId()),
                eq(filter.assignedUserId()),
                eq(filter.status()),
                eq(filter.priority()),
                any(Pageable.class)
        );
    }

}