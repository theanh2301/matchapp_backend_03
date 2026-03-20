package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.QuestionRequest;
import com.company.mathapp_backend_03.model.response.QuestionResponse;
import com.company.mathapp_backend_03.service.LessonService;
import com.company.mathapp_backend_03.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionApi {
    private final QuestionService questionService;
    private final LessonService lessonService;

    @GetMapping("/{lessonId}")
    public List<QuestionResponse> getQuestion(@PathVariable Integer lessonId) {
        return questionService.getQuestionByLessonId(lessonId);
    }

    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody QuestionRequest questionRequest) {
        questionService.addQuestion(questionRequest);
        return ResponseEntity.ok("Question created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Integer id,
                                            @RequestBody QuestionRequest questionRequest) {
        questionService.updateQuestion(id, questionRequest);

        return ResponseEntity.ok("Question updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteQuestion(id);

        return ResponseEntity.ok("Question deleted successfully");
    }

    @PostMapping("/answers")
    public ResponseEntity<?> addQuestionAndAnswer(@Valid @RequestBody QuestionRequest questionRequest) {
        questionService.addQuestionAndAnswer(questionRequest);
        return ResponseEntity.ok("Question and answer created successfully");
    }

    @PutMapping("/{id}/answers")
    public ResponseEntity<?> updateQuestionAndQuestion(@PathVariable Integer id,
                                           @Valid @RequestBody QuestionRequest questionRequest) {
        questionService.updateQuestionAndAnswer(id, questionRequest);

        return ResponseEntity.ok("Question and answer updated successfully");
    }

    @DeleteMapping("/{id}/answers")
    public ResponseEntity<?> deleteQuestionAndAnswer(@PathVariable Integer id) {
        questionService.deleteQuestionAnswer(id);

        return ResponseEntity.ok("Question and answer deleted successfully");
    }
}
