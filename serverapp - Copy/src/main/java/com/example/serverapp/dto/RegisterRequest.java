package com.example.serverapp.dto;

import com.example.serverapp.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}