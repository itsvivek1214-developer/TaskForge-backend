package com.taskforge.dto.response;

import com.taskforge.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .priority(task.getPriority().name())
            .status(task.getStatus().name())
            .deadline(task.getDeadline())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .userId(task.getUser().getId())
            .build();
    }
}
