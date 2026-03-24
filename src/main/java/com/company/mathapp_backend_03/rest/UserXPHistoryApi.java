package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.UserXPHistoryRequest;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.UserXPHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-xp-history")
@RequiredArgsConstructor
public class UserXPHistoryApi {

    private final UserXPHistoryService service;

    @PostMapping
    public ResponseEntity<UserXPHistoryResponse> addXpRecord(@RequestBody UserXPHistoryRequest request) {
        UserXPHistoryResponse response = service.addXpRecord(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserXPHistoryResponse>> getHistoryByUser(@PathVariable Integer userId) {
        List<UserXPHistoryResponse> histories = service.getHistoryByUserId(userId);
        return ResponseEntity.ok(histories);
    }
}