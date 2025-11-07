package org.example.taskmanager.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task requestTask;
    private Task createdTask1;
    private Task createdTask2;
    private Task doneTask1;
    private PageImpl mockPage;
    private List<Task> mockTasks;

    @BeforeEach
    public void init() {

        requestTask = Task.builder()
                .title("test1")
                .creatorId(1L)
                .assignedUserId(2L)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        createdTask1 = Task.builder()
                .id(10L)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.HIGH)
                .build();

        createdTask2 = Task.builder()
                .id(11L)
                .title("test2")
                .creatorId(1L)
                .assignedUserId(2L)
                .status(Status.CREATED)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.LOW)
                .build();

        doneTask1 = Task.builder()
                .id(10L)
                .title("test1")
                .creatorId(1L)
                .assignedUserId(2L)
                .status(Status.DONE)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.HIGH)
                .doneDataTime(LocalDateTime.now())
                .build();

        mockTasks = List.of(createdTask1, createdTask2);
        mockPage = new PageImpl<>(mockTasks);

    }

    @Test
    void taskController_getTaskById_returnTask() throws Exception {

        given(taskService.getTaskById(10L)).willReturn(createdTask1);

        ResultActions response = mockMvc.perform(get("/tasks/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("id", "10"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("10"))
                .andExpect(jsonPath("$.title").value("test1"));
        verify(taskService).getTaskById(10L);
    }

    @Test
    void taskController_getTaskById_returnNotFound() throws Exception {

        given(taskService.getTaskById(99L)).willThrow(new EntityNotFoundException());

        mockMvc.perform(get("/tasks/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taskService).getTaskById(99L);
    }

    @Test
    void taskController_getAllTasks_returnPage() throws Exception {

        given(taskService.getAllTasks(any(Pageable.class))).willReturn(mockPage);

        ResultActions response = mockMvc.perform(get("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].title").value("test1"))
                .andExpect(jsonPath("$.content[1].id").value(11L))
                .andExpect(jsonPath("$.content[1].title").value("test2"));

        verify(taskService).getAllTasks(any(Pageable.class));
    }

    @Test
    void taskController_getAllTasks_returnEmptyPage() throws Exception {

        PageImpl<Task> emptyMockPage = new PageImpl(List.of());

        given(taskService.getAllTasks(any(Pageable.class))).willReturn(emptyMockPage);

        ResultActions response = mockMvc.perform(get("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(taskService).getAllTasks(any(Pageable.class));
    }


    @Test
    void taskController_createNewTask_shouldCreateTaskAndReturn201WithBody() throws Exception {
        given(taskService.createNewTask(any(Task.class))).willReturn(createdTask1);

        ResultActions response = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestTask))
        );

        response.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("test1"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).createNewTask(any(Task.class));
    }

    @Test
    void taskController_editTask_returnUpdatedTask() throws Exception {
        var dataToUpdate = Task.builder()
                .title("test1")
                .creatorId(1L)
                .assignedUserId(2L)
                .deadLineDate(LocalDateTime.now().plusDays(5))
                .priority(Priority.HIGH)
                .build();

        given(taskService.editTask(eq(10L), any(Task.class))).willReturn(createdTask1);

        mockMvc.perform(put("/tasks/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("test1"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(taskService).editTask(eq(10L), any(Task.class));
    }


    @Test
    void taskController_deleteTask_returnOk() throws Exception {
        Long id = 10L;
        willDoNothing().given(taskService).deleteTask(id);

        ResultActions response = mockMvc.perform(delete("/tasks/{id}", id));

        response.andExpect(status().isOk());
        verify(taskService).deleteTask(id);
    }

    @Test
    void taskController_deleteTask_returnNotFound() throws Exception {
        Long id = 10L;
        willThrow(new EntityNotFoundException("There is no task found by ID: " + id))
                .given(taskService).deleteTask(id);

        ResultActions response = mockMvc.perform(delete("/tasks/{id}", id));

        response.andExpect(status().isNotFound());
        verify(taskService).deleteTask(id);
    }


    @Test
    void taskController_switchTaskToInProgress_returnStringAndStatusOk() throws Exception {
        Long id = 10L;
        willDoNothing().given(taskService).switchTaskToInProgress(id);

        ResultActions response = mockMvc.perform(post("/tasks/{id}/start", id));
        response
                .andExpect(status().isOk())
                .andExpect(content().string("Task id = " + id + " successfully switched to IN_PROGRESS"));
        verify(taskService).switchTaskToInProgress(id);
    }

    @Test
    void taskController_switchTaskToInProgress_returnNotFound() throws Exception {
        Long id = 10L;
        willThrow(new EntityNotFoundException("There is no task found by ID: " + id))
                .given(taskService).switchTaskToInProgress(id);

        ResultActions response = mockMvc.perform(post("/tasks/{id}/start", id));

        response.andExpect(status().isNotFound());
        verify(taskService).switchTaskToInProgress(id);
    }

    @Test
    void taskController_switchTaskToInProgress_returnBadRequest() throws Exception {
        Long id = 10L;
        willThrow(new IllegalStateException("User already got many active tasks. Cannot switch to IN_PROGRESS if user has more than 4 active tasks."))
                .given(taskService).switchTaskToInProgress(id);

        ResultActions response = mockMvc.perform(post("/tasks/{id}/start", id));

        response.andExpect(status().isBadRequest());
        verify(taskService).switchTaskToInProgress(id);
    }


    @Test
    void taskController_getAllTasksOfOneUserAssignedUser_returnListTask() throws Exception {
        Long assignedUserId = 2L;

        given(taskService.getAllTasksOfOneAssignedUser(assignedUserId)).willReturn(mockTasks);

        ResultActions response = mockMvc.perform(get("/tasks/user/{assignedUserId}", assignedUserId));
        response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].assignedUserId").value(2L))
                .andExpect(jsonPath("$[1].assignedUserId").value(2L));

        verify(taskService).getAllTasksOfOneAssignedUser(assignedUserId);
    }


    @Test
    void taskController_getTaskDone_returnCompletedTask() throws Exception {
        Long id = 10L;
        given(taskService.getTaskDone(id)).willReturn(doneTask1);

        ResultActions response = mockMvc.perform(post("/tasks/{id}/complete", id));
        response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(taskService).getTaskDone(id);
    }

    @Test
    void taskController_getTaskDone_returnBadRequest() throws Exception {
        Long id = 10L;
        willThrow(new EntityNotFoundException("There is no task found by ID: " + id))
                .given(taskService).getTaskDone(id);

        ResultActions response = mockMvc.perform(post("/tasks/{id}/complete", id));

        response.andExpect(status().isNotFound());
        verify(taskService).getTaskDone(id);
    }


    @Test
    void taskController_searchAllByFilter_createFilterAndReturnListTask() throws Exception {
        given(taskService.searchAllByFilter(any(TaskSearchFilter.class)))
                .willReturn(mockTasks);

        ResultActions response = mockMvc.perform(get("/tasks/filter")
                .param("creatorId", "1")
                .param("assignedUserId", "2")
                .param("status", "CREATED")
                //.param("priority", "HIGH")
                .param("pageSize", "10")
                .param("pageNum", "0")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("CREATED"));

        verify(taskService).searchAllByFilter(any(TaskSearchFilter.class));
    }

    @Test
    void taskController_searchAllByFilter_returnNotFound() throws Exception {

        given(taskService.searchAllByFilter(any(TaskSearchFilter.class)))
                .willThrow(new EntityNotFoundException("Not found task by filter"));

        ResultActions response = mockMvc.perform(get("/tasks/filter")
                .param("creatorId", "1")
                .param("assignedUserId", "2")
                .param("status", "CREATED")
                .param("priority", "HIGH")
                .param("pageSize", "10")
                .param("pageNum", "0")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound());
        verify(taskService).searchAllByFilter(any(TaskSearchFilter.class));
    }
}
