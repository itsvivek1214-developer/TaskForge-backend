package com.taskforge.service;

import com.taskforge.dto.request.TaskRequest;
import com.taskforge.dto.response.AIResponse;
import com.taskforge.entity.Task.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AIService {

    // ── Priority Engine ───────────────────────────────────────
    // Rule-based: deadline proximity + description complexity

    public Priority calculatePriority(LocalDateTime deadline, String description) {
        if (deadline == null) {
            return estimateFromComplexity(description);
        }

        long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);

        if (hoursUntilDeadline < 0)  return Priority.HIGH;   // Overdue
        if (hoursUntilDeadline < 24) return Priority.HIGH;   // Due within 24 hours
        if (hoursUntilDeadline < 72) return Priority.MEDIUM; // Due within 3 days

        // If deadline is far but description is complex, bump to MEDIUM
        if (isComplexTask(description)) return Priority.MEDIUM;

        return Priority.LOW;
    }

    public AIResponse.Prioritize prioritize(TaskRequest.AIPrioritize request) {
        Priority priority = calculatePriority(request.getDeadline(), request.getDescription());
        String reasoning  = buildReasoning(request.getDeadline(), priority, request.getDescription());
        int confidence    = calculateConfidence(request.getDeadline(), request.getDescription());

        log.info("AI prioritize: title='{}' → {}", request.getTitle(), priority);
        return AIResponse.Prioritize.builder()
            .priority(priority.name())
            .reasoning(reasoning)
            .confidence(confidence)
            .build();
    }

    // ── Suggestion Engine ─────────────────────────────────────

    public AIResponse.Suggest suggest(TaskRequest.AISuggest request) {
        String title       = request.getTitle() != null ? request.getTitle() : "";
        String description = request.getDescription() != null ? request.getDescription() : "";

        String suggestedTitle      = optimizeTitle(title, description);
        String summary             = generateSummary(title, description);
        LocalDateTime deadline     = recommendDeadline(description);
        List<String> tips          = generateTips(title, description);

        log.info("AI suggest: title='{}'", title);
        return AIResponse.Suggest.builder()
            .suggestedTitle(suggestedTitle)
            .summary(summary)
            .recommendedDeadline(deadline)
            .tips(tips)
            .build();
    }

    // ── Private helpers ───────────────────────────────────────

    private Priority estimateFromComplexity(String description) {
        if (isComplexTask(description)) return Priority.MEDIUM;
        return Priority.LOW;
    }

    private boolean isComplexTask(String description) {
        if (description == null || description.isBlank()) return false;
        int wordCount = description.trim().split("\\s+").length;
        // Consider "complex" if description is detailed (>30 words) or contains
        // keywords suggesting urgency/complexity
        boolean longDescription = wordCount > 30;
        boolean complexKeywords = description.toLowerCase().matches(
            ".*(critical|urgent|blocker|asap|integration|migration|deployment|refactor|architecture).*"
        );
        return longDescription || complexKeywords;
    }

    private String buildReasoning(LocalDateTime deadline, Priority priority, String description) {
        if (deadline == null) {
            if (isComplexTask(description)) {
                return "No deadline provided, but the task description indicates high complexity — MEDIUM priority assigned.";
            }
            return "No deadline provided and task appears straightforward — defaulting to LOW priority.";
        }

        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);

        if (hours < 0) {
            return String.format("Task is overdue by %d hour(s). Immediate attention required — HIGH priority.", Math.abs(hours));
        }
        if (hours < 24) {
            return String.format("Deadline is in %d hour(s). Critical urgency threshold reached — HIGH priority assigned.", hours);
        }
        if (hours < 72) {
            long days = hours / 24;
            return String.format("Deadline is in approximately %d day(s). Approaching soon — MEDIUM priority to ensure timely completion.", days);
        }
        long days = hours / 24;
        return String.format("Deadline is %d days away. Sufficient time available — LOW priority for now. Reassess as the deadline approaches.", days);
    }

    private int calculateConfidence(LocalDateTime deadline, String description) {
        // Higher confidence when we have more signals
        int score = 60; // base
        if (deadline != null)  score += 25;
        if (description != null && description.length() > 20) score += 10;
        if (description != null && description.length() > 100) score += 5;
        return Math.min(score, 98);
    }

    private String optimizeTitle(String title, String description) {
        if (title.isBlank() && description.isBlank()) return "New Task";

        String base = title.isBlank()
            ? capitalize(description.substring(0, Math.min(description.length(), 60)).trim())
            : title.trim();

        // Add action prefix if missing
        String lower = base.toLowerCase();
        boolean hasActionVerb = lower.startsWith("implement") || lower.startsWith("fix") ||
            lower.startsWith("create") || lower.startsWith("update") || lower.startsWith("add") ||
            lower.startsWith("remove") || lower.startsWith("refactor") || lower.startsWith("test") ||
            lower.startsWith("deploy") || lower.startsWith("review") || lower.startsWith("write") ||
            lower.startsWith("build") || lower.startsWith("design") || lower.startsWith("migrate");

        if (!hasActionVerb) {
            if (lower.contains("bug") || lower.contains("fix") || lower.contains("error")) {
                return "Fix: " + base;
            } else if (lower.contains("test") || lower.contains("spec")) {
                return "Test: " + base;
            } else {
                return "Implement: " + base;
            }
        }

        return base;
    }

    private String generateSummary(String title, String description) {
        if (!description.isBlank()) {
            int wordCount = description.trim().split("\\s+").length;
            if (wordCount <= 20) {
                return description.trim();
            }
            // Trim to first 2 sentences or 150 chars
            String trimmed = description.length() > 150
                ? description.substring(0, 150).trim() + "..."
                : description.trim();
            return "This task involves: " + trimmed +
                " Recommended to break into sub-tasks for better tracking.";
        }
        if (!title.isBlank()) {
            return "Complete the work described in \"" + title.trim() +
                "\". Ensure requirements are clarified before starting.";
        }
        return "A new task requiring attention. Add a description to get a better AI summary.";
    }

    private LocalDateTime recommendDeadline(String description) {
        if (isComplexTask(description)) {
            return LocalDateTime.now().plusDays(5); // 5 days for complex tasks
        }
        int wordCount = (description != null) ? description.trim().split("\\s+").length : 0;
        if (wordCount > 15) {
            return LocalDateTime.now().plusDays(3); // 3 days for medium tasks
        }
        return LocalDateTime.now().plusDays(2); // 2 days for simple tasks
    }

    private List<String> generateTips(String title, String description) {
        List<String> tips = new ArrayList<>();
        String combined = (title + " " + description).toLowerCase();

        tips.add("Break this task into smaller, trackable sub-tasks if it takes more than 2 hours.");

        if (combined.contains("api") || combined.contains("integration") || combined.contains("service")) {
            tips.add("Identify all API contracts and dependencies before starting implementation.");
        }
        if (combined.contains("database") || combined.contains("migration") || combined.contains("schema")) {
            tips.add("Always back up data before running database migrations.");
        }
        if (combined.contains("test") || combined.contains("bug") || combined.contains("fix")) {
            tips.add("Write a failing test first to confirm the bug before fixing it.");
        }
        if (combined.contains("deploy") || combined.contains("release") || combined.contains("production")) {
            tips.add("Ensure a rollback plan is in place before deploying to production.");
        }
        if (combined.contains("review") || combined.contains("audit") || combined.contains("check")) {
            tips.add("Prepare a checklist to ensure nothing is missed during the review.");
        }

        // Always add a general tip
        tips.add("Schedule a mid-point check-in at 50% completion to catch blockers early.");

        return tips.subList(0, Math.min(tips.size(), 3)); // Return max 3 tips
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
