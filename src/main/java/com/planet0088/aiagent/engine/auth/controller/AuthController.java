package com.planet0088.aiagent.engine.auth.controller;

import com.planet0088.aiagent.engine.auth.dto.LoginRequest;
import com.planet0088.aiagent.engine.auth.dto.LoginResponse;
import com.planet0088.aiagent.engine.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
