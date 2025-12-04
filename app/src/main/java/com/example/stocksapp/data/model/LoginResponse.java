package com.example.stocksapp.data.model;

// 로그인 응답 바디
public class LoginResponse {

    private User user;
    private String message;

    public LoginResponse() {
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    // 내부 User 모델 (응답 안의 "user" 객체)
    public static class User {
        private String email;
        private String username;
        private int id;
        private boolean is_active;

        public User() {
        }

        public String getEmail() {
            return email;
        }

        public String getUsername() {
            return username;
        }

        public int getId() {
            return id;
        }

        public boolean isIs_active() {   // is_active 그대로 매핑
            return is_active;
        }
    }
}
