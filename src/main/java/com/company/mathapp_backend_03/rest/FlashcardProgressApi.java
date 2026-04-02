package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.FlashcardProgressService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
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

    @PostMapping("/progress/batch")
    public ResponseEntity<Map<String, Object>> saveFlashcardBatch(
            @RequestBody @Valid List<FlashcardProgressRequest> requests
    ) {

        flashcardProgressService.processFlashcardStudyBatch(requests);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lưu thành công");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/progress")
    public ResponseEntity<ApiResponse<UserXPHistoryResponse>> submitFlashcardProgress(
            @RequestBody FlashcardProgressRequest request) {

        ApiResponse<UserXPHistoryResponse> response = new ApiResponse<>();

        try {
            // Gọi logic xử lý từ Service
            UserXPHistoryResponse result = flashcardProgressService.processFlashcardStudy(request);

            // Đóng gói thành công
            response.setStatus(HttpStatus.OK.value()); // 200

            if (result.getXp() > 0) {
                response.setMessage("Đã lưu tiến độ thẻ. Nhận được " + result.getXp() + " XP!");
            } else {
                response.setMessage("Đã lưu tiến độ thẻ. Không nhận thêm XP.");
            }
            response.setData(result);

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            // Lỗi không tìm thấy User hoặc Flashcard (404)
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalStateException e) {
            // Lỗi thao tác quá nhanh hoặc vi phạm dữ liệu (400)
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            // Các lỗi hệ thống khác (500)
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Lỗi hệ thống: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
