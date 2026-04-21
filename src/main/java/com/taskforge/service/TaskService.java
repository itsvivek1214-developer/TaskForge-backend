package com.taskforge.service;

import com.taskforge.dto.request.TaskRequest;
import com.taskforge.dto.response.TaskResponse;
import com.taskforge.entity.Task;
import com.taskforge.entity.Task.Status;
import com.taskforge.entity.Task.Priority;
import com.taskforge.entity.User;
import com.taskforge.exception.GlobalExceptionHandler.*;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    // ── Helpers ───────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Task getTaskForUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (!task.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to access this task");
        }
        return task;
    }

    // ── Get all tasks (paginated + filtered) ──────────────────

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(
            String email, boolean isAdmin,
            String statusStr, String priorityStr, String search,
            int page, int size, String sortBy, String sortDir) {

        Status status     = parseEnum(Status.class,   statusStr);
        Priority priority = parseEnum(Priority.class, priorityStr);
        String searchTerm = (search != null && search.isBlank()) ? null : search;

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks;
        if (isAdmin) {
            tasks = taskRepository.findAllWithFilters(status, priority, searchTerm, pageable);
        } else {
            User user = getUser(email);
            tasks = taskRepository.findByUserIdWithFilters(user.getId(), status, priority, searchTerm, pageable);
        }

        return tasks.map(TaskResponse::from);
    }

    // ── Get single task ───────────────────────────────────────

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, String email, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (!isAdmin && !task.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("You do not have permission to access this task");
        }
        return TaskResponse.from(task);
    }

    // ── Create task ───────────────────────────────────────────

    @Transactional
    public TaskResponse createTask(TaskRequest.Create request, String email) {
        User user = getUser(email);

        // AI auto-prioritize if no deadline is set and priority is still default
        Priority priority = request.getPriority();
        if (priority == null) {
            priority = aiService.calculatePriority(request.getDeadline(), request.getDescription());
        }

        Task task = Task.builder()
            .title(request.getTitle().trim())
            .description(request.getDescription() != null ? request.getDescription().trim() : null)
            .priority(priority)
            .status(request.getStatus() != null ? request.getStatus() : Status.TODO)
            .deadline(request.getDeadline())
            .user(user)
            .build();

        Task saved = taskRepository.save(task);
        log.info("Task created [id={}] by user [{}]", saved.getId(), email);
        return TaskResponse.from(saved);
    }

    // ── Update task ───────────────────────────────────────────

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest.Update request, String email, boolean isAdmin) {
        User user = getUser(email);
        Task task = isAdmin
            ? taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId))
            : getTaskForUser(taskId, user.getId());

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus()   != null) validateAndSetStatus(task, request.getStatus());
        task.setDeadline(request.getDeadline());

        Task saved = taskRepository.save(task);
        log.info("Task updated [id={}] by user [{}]", taskId, email);
        return TaskResponse.from(saved);
    }

    // ── Delete task ───────────────────────────────────────────

    @Transactional
    public void deleteTask(Long taskId, String email, boolean isAdmin) {
        User user = getUser(email);
        Task task = isAdmin
            ? taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId))
            : getTaskForUser(taskId, user.getId());
        taskRepository.delete(task);
        log.info("Task deleted [id={}] by user [{}]", taskId, email);
    }

    // ── Update status (workflow) ──────────────────────────────

    @Transactional
    public TaskResponse updateStatus(Long taskId, TaskRequest.UpdateStatus request, String email, boolean isAdmin) {
        User user = getUser(email);
        Task task = isAdmin
            ? taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId))
            : getTaskForUser(taskId, user.getId());

        validateAndSetStatus(task, request.getStatus());
        Task saved = taskRepository.save(task);
        log.info("Task [id={}] status updated to {} by [{}]", taskId, request.getStatus(), email);
        return TaskResponse.from(saved);
    }

    // ── Workflow validation ───────────────────────────────────
    // Allowed: TODO→IN_PROGRESS, IN_PROGRESS→DONE, IN_PROGRESS→TODO
    // Blocked: DONE→TODO (direct), DONE→IN_PROGRESS

    private void validateAndSetStatus(Task task, Status newStatus) {
        Status current = task.getStatus();
        if (current == newStatus) return;

        boolean valid = switch (current) {
            case TODO        -> newStatus == Status.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == Status.DONE || newStatus == Status.TODO;
            case DONE        -> false; // Cannot move out of DONE directly
        };

        if (!valid) {
            throw new BadRequestException(
                String.format("Invalid status transition: %s → %s. " +
                    "Allowed: TODO→IN_PROGRESS, IN_PROGRESS→DONE or TODO. DONE is terminal.",
                    current, newStatus)
            );
        }
        task.setStatus(newStatus);
    }

    // ── Utility ───────────────────────────────────────────────

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid value '" + value + "' for " + enumClass.getSimpleName());
        }
    }
}
