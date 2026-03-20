package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.request.LessonRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.LessonResponse;
import com.company.mathapp_backend_03.service.FlashcardProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard_progress")
@RequiredArgsConstructor
public class FlashcardProgressApi {
    private final FlashcardProgressService flashcardProgressService;

    @GetMapping("/{userId}/{flashcardId}")
    public List<FlashcardProgressResponse> getFlashcardProgress(@PathVariable Integer userId,
                                                      @PathVariable Integer flashcardId) {
        return flashcardProgressService.getFlashcardProgressByFlashcardIdAndUserId(userId, flashcardId);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveProgress(@Valid @RequestBody FlashcardProgressRequest request) {
        flashcardProgressService.addOrUpdateFlashcardProgress(request);
        return ResponseEntity.ok("Cập nhật tiến độ học tập thành công!");
    }



}
