package com.taskforge.controller;

import com.taskforge.dto.request.TaskRequest;
import com.taskforge.dto.response.AIResponse;
import com.taskforge.dto.response.TaskResponse;
import com.taskforge.service.AIService;
import com.taskforge.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AIService   aiService;

    // ── Helper ────────────────────────────────────────────────

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ── GET /api/tasks ────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir) {

        Page<TaskResponse> tasks = taskService.getAllTasks(
            userDetails.getUsername(), isAdmin(userDetails),
            status, priority, search, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(tasks);
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            taskService.getTaskById(id, userDetails.getUsername(), isAdmin(userDetails))
        );
    }

    // ── POST /api/tasks ───────────────────────────────────────

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest.Create request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(taskService.createTask(request, userDetails.getUsername()));
    }

    // ── PUT /api/tasks/{id} ───────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest.Update request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            taskService.updateTask(id, request, userDetails.getUsername(), isAdmin(userDetails))
        );
    }

    // ── DELETE /api/tasks/{id} ────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername(), isAdmin(userDetails));
        return ResponseEntity.noContent().build();
    }

    // ── PATCH /api/tasks/{id}/status ─────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody TaskRequest.UpdateStatus request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            taskService.updateStatus(id, request, userDetails.getUsername(), isAdmin(userDetails))
        );
    }

    // ── POST /api/tasks/prioritize ────────────────────────────

    @PostMapping("/prioritize")
    public ResponseEntity<AIResponse.Prioritize> prioritize(
            @RequestBody TaskRequest.AIPrioritize request) {
        return ResponseEntity.ok(aiService.prioritize(request));
    }

    // ── POST /api/tasks/suggest ───────────────────────────────

    @PostMapping("/suggest")
    public ResponseEntity<AIResponse.Suggest> suggest(
            @RequestBody TaskRequest.AISuggest request) {
        return ResponseEntity.ok(aiService.suggest(request));
    }
}
