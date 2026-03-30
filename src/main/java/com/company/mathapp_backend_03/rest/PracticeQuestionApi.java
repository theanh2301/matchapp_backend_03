package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.enums.Difficulty;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.PracticeQuestionResponse;
import com.company.mathapp_backend_03.model.response.QuizQuestionResponse;
import com.company.mathapp_backend_03.repository.PracticeQuestionRepository;
import com.company.mathapp_backend_03.service.PracticeQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practices")
@RequiredArgsConstructor
public class PracticeQuestionApi {

    private final PracticeQuestionService practiceQuestionService;

    @GetMapping("/{practiceId}")
    public ResponseEntity<ApiResponse<List<PracticeQuestionResponse>>> getPracticeQuestion(
            @PathVariable Integer practiceId) {

        List<PracticeQuestionResponse> questions = practiceQuestionService.getPracticeQuestionByPracticeId(practiceId);

        ApiResponse<List<PracticeQuestionResponse>> response = new ApiResponse<>(
                200,
                "Get practice card successfully",
                questions
        );

        return ResponseEntity.ok(response);
    }
}
