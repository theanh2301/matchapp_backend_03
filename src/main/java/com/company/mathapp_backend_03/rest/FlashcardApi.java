package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.FlashcardRequest;
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
    public List<FlashcardResponse> getFlashcards(@PathVariable Integer lessonId) {
        return flashcardService.getFlashcard(lessonId);
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
