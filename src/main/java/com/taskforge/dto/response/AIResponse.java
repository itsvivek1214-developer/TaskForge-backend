package com.taskforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AIResponse {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Prioritize {
        private String priority;
        private String reasoning;
        private int confidence;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Suggest {
        private String suggestedTitle;
        private String summary;
        private LocalDateTime recommendedDeadline;
        private List<String> tips;
    }
}
