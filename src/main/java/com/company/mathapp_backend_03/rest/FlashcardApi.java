package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.entity.Flashcard;
import com.company.mathapp_backend_03.model.request.FlashcardRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.FlashcardResponse;
import com.company.mathapp_backend_03.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardApi {

    private final FlashcardService flashcardService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcards(@PathVariable Integer lessonId) {

        List<FlashcardResponse> flashcards = flashcardService.getFlashcard(lessonId);

        ApiResponse<List<FlashcardResponse>> response = new ApiResponse<>(
                200,
                "Get set flashcard successfully",
                flashcards
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addFlashcard (@Valid @RequestBody FlashcardRequest flashcardRequest) {
        flashcardService.addFlashcard(flashcardRequest);
        return ResponseEntity.ok("Flashcard created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlashcard(@PathVariable Integer id,
                                            @Valid @RequestBody FlashcardRequest flashcardRequest) {
        flashcardService.updateFlashcard(id, flashcardRequest);

        return ResponseEntity.ok("Flashcard updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFlashcard(@PathVariable Integer id) {
        flashcardService.deleteFlashcard(id);

        return ResponseEntity.ok("Flashcard deleted successfully");
    }

}
