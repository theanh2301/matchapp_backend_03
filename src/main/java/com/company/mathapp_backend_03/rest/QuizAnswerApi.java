package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.ListAnswerRequest;
import com.company.mathapp_backend_03.model.response.QuizAnswerResponse;
import com.company.mathapp_backend_03.service.QuizAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class QuizAnswerApi {
    private final QuizAnswerService quizAnswerService;

    @GetMapping("/{questionId}")
    public List<QuizAnswerResponse> getAnswersByQuestionId(@PathVariable Integer questionId) {
        return quizAnswerService.getAnswersByQuestionId(questionId);
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> addAnswers(@PathVariable Integer questionId, @Valid @RequestBody ListAnswerRequest request) {

        request.setQuestionId(questionId);
        quizAnswerService.addAnswers(request);

        return ResponseEntity.ok("Answers created successfully");
    }

    @PutMapping("/{questionId}/answers")
    public ResponseEntity<?> updateAnswers(@PathVariable Integer questionId, @Valid @RequestBody ListAnswerRequest request) {

        request.setQuestionId(questionId);
        quizAnswerService.updateAnswers(request);

        return ResponseEntity.ok("Answers updated successfully");
    }

    @DeleteMapping("/{questionId}/answers")
    public ResponseEntity<?> deleteAnswers(@PathVariable Integer questionId) {
        quizAnswerService.deleteAnswersByQuestion(questionId);
        return ResponseEntity.ok("Answers deleted successfully");
    }

}
