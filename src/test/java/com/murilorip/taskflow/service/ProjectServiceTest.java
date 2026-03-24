package com.murilorip.taskflow.service;

import com.murilorip.taskflow.dto.ProjectRequest;
import com.murilorip.taskflow.dto.ProjectResponse;
import com.murilorip.taskflow.entity.Project;
import com.murilorip.taskflow.entity.Role;
import com.murilorip.taskflow.entity.TaskStatus;
import com.murilorip.taskflow.entity.User;
import com.murilorip.taskflow.exception.DuplicateResourceException;
import com.murilorip.taskflow.exception.ResourceNotFoundException;
import com.murilorip.taskflow.repository.ProjectRepository;
import com.murilorip.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("A test project")
                .owner(testUser)
                .tasks(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should create project successfully")
    void shouldCreateProjectSuccessfully() {
        ProjectRequest request = ProjectRequest.builder()
                .name("New Project")
                .description("Project description")
                .build();

        when(projectRepository.existsByNameAndOwnerId("New Project", 1L)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.DONE)).thenReturn(0L);

        ProjectResponse response = projectService.createProject(request, testUser);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Project");
        assertThat(response.getOwnerName()).isEqualTo("John Doe");
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("should throw exception when creating duplicate project")
    void shouldThrowWhenCreatingDuplicate() {
        ProjectRequest request = ProjectRequest.builder()
                .name("Existing Project")
                .build();

        when(projectRepository.existsByNameAndOwnerId("Existing Project", 1L)).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request, testUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("should get project by ID")
    void shouldGetProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.countByProjectIdAndStatus(1L, TaskStatus.DONE)).thenReturn(0L);

        ProjectResponse response = projectService.getProjectById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw exception when project not found")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should delete project successfully")
    void shouldDeleteProjectSuccessfully() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        doNothing().when(projectRepository).delete(testProject);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).delete(testProject);
    }
}
