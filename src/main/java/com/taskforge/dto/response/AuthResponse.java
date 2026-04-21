package com.taskforge.dto.response;

import com.taskforge.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthResponse {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Login {
        private String token;
        private UserInfo user;

        @Data @Builder @NoArgsConstructor @AllArgsConstructor
        public static class UserInfo {
            private Long id;
            private String name;
            private String email;
            private String role;
        }

        public static Login from(String token, User user) {
            return Login.builder()
                .token(token)
                .user(UserInfo.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build())
                .build();
        }
    }

    @Data @AllArgsConstructor
    public static class Message {
        private String message;
    }
}
