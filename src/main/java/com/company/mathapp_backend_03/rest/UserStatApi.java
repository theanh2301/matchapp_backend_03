package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.UserStatRequest;
import com.company.mathapp_backend_03.model.response.UserStatResponse;
import com.company.mathapp_backend_03.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-stats")
@RequiredArgsConstructor
public class UserStatApi {

    private final UserStatService service;

    @GetMapping("/{userId}")
    public ResponseEntity<UserStatResponse> getStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(service.getUserStats(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserStatResponse> updateStats(
            @PathVariable Integer userId,
            @RequestBody UserStatRequest request) {
        return ResponseEntity.ok(service.updateStats(userId, request));
    }
}