package com.planet0088.aiagent.engine.auth.service;

import com.planet0088.aiagent.engine.auth.dto.LoginRequest;
import com.planet0088.aiagent.engine.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
