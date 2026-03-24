package com.murilorip.taskflow.service;

import com.murilorip.taskflow.dto.TaskRequest;
import com.murilorip.taskflow.dto.TaskResponse;
import com.murilorip.taskflow.entity.*;
import com.murilorip.taskflow.exception.ResourceNotFoundException;
import com.murilorip.taskflow.repository.ProjectRepository;
import com.murilorip.taskflow.repository.TaskRepository;
import com.murilorip.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("A test project")
                .owner(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("A test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .project(testProject)
                .assignee(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskById {

        @Test
        @DisplayName("should return task when found")
        void shouldReturnTaskWhenFound() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            TaskResponse response = taskService.getTaskById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Task");
            assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(response.getProjectName()).isEqualTo("Test Project");
            assertThat(response.getAssigneeName()).isEqualTo("John Doe");
            verify(taskRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowExceptionWhenNotFound() {
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTaskById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found");
        }
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully")
        void shouldCreateTaskSuccessfully() {
            TaskRequest request = TaskRequest.builder()
                    .title("New Task")
                    .description("New task description")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .projectId(1L)
                    .assigneeId(1L)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            TaskResponse response = taskService.createTask(request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Test Task");
            verify(projectRepository, times(1)).findById(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when project not found")
        void shouldThrowWhenProjectNotFound() {
            TaskRequest request = TaskRequest.builder()
                    .title("New Task")
                    .projectId(999L)
                    .build();

            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project not found");
        }

        @Test
        @DisplayName("should create task without assignee")
        void shouldCreateTaskWithoutAssignee() {
            TaskRequest request = TaskRequest.builder()
                    .title("Unassigned Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .projectId(1L)
                    .build();

            Task unassignedTask = Task.builder()
                    .id(2L)
                    .title("Unassigned Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .project(testProject)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(taskRepository.save(any(Task.class))).thenReturn(unassignedTask);

            TaskResponse response = taskService.createTask(request);

            assertThat(response).isNotNull();
            assertThat(response.getAssigneeId()).isNull();
            assertThat(response.getAssigneeName()).isNull();
            verify(userRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("getTasksByProject")
    class GetTasksByProject {

        @Test
        @DisplayName("should return paginated tasks for a project")
        void shouldReturnPaginatedTasks() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> taskPage = new PageImpl<>(List.of(testTask), pageable, 1);

            when(taskRepository.findByProjectId(1L, pageable)).thenReturn(taskPage);

            Page<TaskResponse> result = taskService.getTasksByProject(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Task");
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("should delete task successfully")
        void shouldDeleteTaskSuccessfully() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            doNothing().when(taskRepository).delete(testTask);

            taskService.deleteTask(1L);

            verify(taskRepository, times(1)).delete(testTask);
        }

        @Test
        @DisplayName("should throw exception when deleting non-existent task")
        void shouldThrowWhenDeletingNonExistent() {
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
