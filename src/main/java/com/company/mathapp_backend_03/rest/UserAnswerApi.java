package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.UserAnswerRequest;
import com.company.mathapp_backend_03.model.response.UserAnswerResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.QuizQuestionService;
import com.company.mathapp_backend_03.service.UserAnswerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class UserAnswerApi {

    private final UserAnswerService userAnswerService;
    private final QuizQuestionService quizQuestionService;

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

    @PostMapping("/progress")
    public ResponseEntity<?> submitUserAnswerXp(@RequestBody UserAnswerRequest request) {

        try {
            UserXPHistoryResponse xpResponse = userAnswerService.processUserAnswer(request);

            Map<String, Object> response = new HashMap<>();

            if (xpResponse.getXp() == 0) {
                response.put("message", "Câu trả lời đã được lưu (không có XP).");
            } else {
                response.put("message", "Trả lời chính xác! Bạn được cộng XP.");
            }

            response.put("data", xpResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", e.getMessage()
            ));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "error", "Internal Server Error"
            ));
        }
    }
}