package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.request.UserStatRequest;
import com.company.mathapp_backend_03.model.response.UserStatResponse;
import com.company.mathapp_backend_03.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserStatApi {

    private final UserStatService service;

    @GetMapping("/stat/{userId}")
    public ResponseEntity<UserStatResponse> getStats(@PathVariable Integer userId) {
        return ResponseEntity.ok(service.getUserStats(userId));
    }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboard(
            @PathVariable Integer userId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date) {

        LocalDate targetDate = (date == null) ? LocalDate.now() : date;

        if (targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }

        return ResponseEntity.ok(
                service.getDashboard(userId, targetDate)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserStatResponse> updateStats(
            @PathVariable Integer userId,
            @RequestBody UserStatRequest request) {
        return ResponseEntity.ok(service.updateStats(userId, request));
    }
}