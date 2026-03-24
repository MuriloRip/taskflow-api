package com.murilorip.taskflow.service;

import com.murilorip.taskflow.dto.ProjectRequest;
import com.murilorip.taskflow.dto.ProjectResponse;
import com.murilorip.taskflow.entity.Project;
import com.murilorip.taskflow.entity.TaskStatus;
import com.murilorip.taskflow.entity.User;
import com.murilorip.taskflow.exception.DuplicateResourceException;
import com.murilorip.taskflow.exception.ResourceNotFoundException;
import com.murilorip.taskflow.repository.ProjectRepository;
import com.murilorip.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public Page<ProjectResponse> getProjectsByOwner(Long ownerId, Pageable pageable) {
        return projectRepository.findByOwnerId(ownerId, pageable)
                .map(this::toResponse);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = findProjectOrThrow(id);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User owner) {
        if (projectRepository.existsByNameAndOwnerId(request.getName(), owner.getId())) {
            throw new DuplicateResourceException(
                    "Project with name '" + request.getName() + "' already exists");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = findProjectOrThrow(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectOrThrow(id);
        projectRepository.delete(project);
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private ProjectResponse toResponse(Project project) {
        int taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        long completedCount = taskRepository.countByProjectIdAndStatus(
                project.getId(), TaskStatus.DONE);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerName(project.getOwner().getName())
                .taskCount(taskCount)
                .completedTaskCount((int) completedCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
