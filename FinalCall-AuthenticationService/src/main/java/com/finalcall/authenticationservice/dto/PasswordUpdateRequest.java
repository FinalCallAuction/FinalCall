// src/main/java/com/finalcall/authenticationservice/dto/PasswordUpdateRequest.java

package com.finalcall.authenticationservice.dto;

public class PasswordUpdateRequest {
    private String newPassword;

    public PasswordUpdateRequest() {}

    public PasswordUpdateRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    // Getter and Setter

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
