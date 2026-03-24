package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.UserAnswerRequest;
import com.company.mathapp_backend_03.model.response.UserAnswerResponse;
import com.company.mathapp_backend_03.service.UserAnswerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-answers")
@RequiredArgsConstructor
public class UserAnswerApi {

    private final UserAnswerService userAnswerService;

    @GetMapping("/detail")
    public ResponseEntity<UserAnswerResponse> getUserAnswer(
            @RequestParam Integer userId,
            @RequestParam Integer questionId) {

        UserAnswerResponse response = userAnswerService.getUserAnswer(userId, questionId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitUserAnswer(@Valid @RequestBody UserAnswerRequest request) {
        try {
            userAnswerService.addOrUpdateUserAnswer(request);

            return ResponseEntity.ok("Ghi nhận đáp án thành công!");

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống.");
        }
    }
}