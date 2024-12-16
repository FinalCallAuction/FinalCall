// src/main/java/com/finalcall/authenticationservice/dto/LoginRequest.java

package com.finalcall.authenticationservice.dto;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // CHANGE ME OH THIS IS AWFULLY UNSAFE
    public void setPassword(String password) {
        this.password = password;
    }
}
