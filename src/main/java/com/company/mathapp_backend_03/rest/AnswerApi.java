package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.ListAnswerRequest;
import com.company.mathapp_backend_03.model.response.AnswerResponse;
import com.company.mathapp_backend_03.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerApi {
    private final AnswerService answerService;

    @GetMapping("/{questionId}")
    public List<AnswerResponse> getAnswersByQuestionId(@PathVariable Integer questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> addAnswers(@PathVariable Integer questionId, @Valid @RequestBody ListAnswerRequest request) {

        request.setQuestionId(questionId);
        answerService.addAnswers(request);

        return ResponseEntity.ok("Answers created successfully");
    }

    @PutMapping("/{questionId}/answers")
    public ResponseEntity<?> updateAnswers(@PathVariable Integer questionId, @Valid @RequestBody ListAnswerRequest request) {

        request.setQuestionId(questionId);
        answerService.updateAnswers(request);

        return ResponseEntity.ok("Answers updated successfully");
    }

    @DeleteMapping("/{questionId}/answers")
    public ResponseEntity<?> deleteAnswers(@PathVariable Integer questionId) {
        answerService.deleteAnswersByQuestion(questionId);
        return ResponseEntity.ok("Answers deleted successfully");
    }

}
