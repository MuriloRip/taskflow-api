package com.murilorip.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private int taskCount;
    private int completedTaskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
