package com.taskforge.dto.request;

import com.taskforge.entity.Task.Priority;
import com.taskforge.entity.Task.Status;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public class TaskRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        private String title;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        private Priority priority = Priority.LOW;

        private Status status = Status.TODO;

        private LocalDateTime deadline;
    }

    @Data
    public static class Update {
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        private String title;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;

        private Priority priority;

        private Status status;

        private LocalDateTime deadline;
    }

    @Data
    public static class UpdateStatus {
        private Status status;
    }

    @Data
    public static class AIPrioritize {
        private String title;
        private String description;
        private LocalDateTime deadline;
    }

    @Data
    public static class AISuggest {
        private String title;
        private String description;
    }
}
