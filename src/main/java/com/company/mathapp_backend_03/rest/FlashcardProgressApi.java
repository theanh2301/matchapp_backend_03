package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.service.FlashcardProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard_progress")
@RequiredArgsConstructor
public class FlashcardProgressApi {
    private final FlashcardProgressService flashcardProgressService;

    @GetMapping("/{userId}/{flashcardId}")
    public ResponseEntity<FlashcardProgressResponse> getFlashcardProgress(@PathVariable Integer userId,
                                                      @PathVariable Integer flashcardId) {
        FlashcardProgressResponse response = flashcardProgressService.getFlashcardProgress(userId, flashcardId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveProgress(@Valid @RequestBody FlashcardProgressRequest request) {
        flashcardProgressService.addOrUpdateFlashcardProgress(request);
        return ResponseEntity.ok("Progress updated successfully");
    }



}
