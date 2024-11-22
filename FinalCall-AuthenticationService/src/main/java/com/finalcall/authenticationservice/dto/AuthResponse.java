// src/main/java/com/finalcall/authenticationservice/dto/AuthResponse.java

package com.finalcall.authenticationservice.dto;

public class AuthResponse {
    private String token;

    public AuthResponse() {}

    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter and Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
