package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.model.request.LoginRequest;
import com.company.mathapp_backend_03.model.request.RegisterRequest;
import com.company.mathapp_backend_03.model.response.UserResponse;
import com.company.mathapp_backend_03.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserApi {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }


}
