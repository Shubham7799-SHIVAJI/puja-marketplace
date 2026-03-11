package com.SHIVA.puja.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {

        userService.loginUser(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User saved successfully");

        return response;
    }

}