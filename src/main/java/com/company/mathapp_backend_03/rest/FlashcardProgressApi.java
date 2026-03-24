package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.FlashcardProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @PostMapping("/progress")
    public ResponseEntity<Map<String, Object>> submitFlashcardProgress(@RequestBody FlashcardProgressRequest request) {

        // Gọi service xử lý logic và cộng điểm
        UserXPHistoryResponse xpResponse = flashcardProgressService.processFlashcardStudy(request);

        Map<String, Object> response = new HashMap<>();

        if (xpResponse == null) {
            // Trả về 200 OK nhưng báo cho Flutter biết là không có XP mới
            response.put("message", "Đã lưu tiến độ thẻ. Không nhận thêm XP.");
            response.put("data", null);
        } else {
            // Có XP mới được cộng
            response.put("message", "Học thẻ thành công! Bạn nhận được điểm thưởng.");
            response.put("data", xpResponse);
        }

        return ResponseEntity.ok(response);
    }
}
