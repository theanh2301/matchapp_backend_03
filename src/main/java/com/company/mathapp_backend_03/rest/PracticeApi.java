package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.entity.PracticeProgress;
import com.company.mathapp_backend_03.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import com.company.mathapp_backend_03.model.request.PracticeProgressRequest;
import com.company.mathapp_backend_03.model.request.UserAnswerRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.PracticeProgressService;
import com.company.mathapp_backend_03.service.PracticeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practices")
@AllArgsConstructor
public class PracticeApi {

    private final PracticeService practiceService;
    private final PracticeProgressService practiceProgressService;

    @GetMapping("/stats")
    public ResponseEntity<PracticeStatsResponse> getStatsByType(
            @RequestParam PracticeType practiceType,
            @RequestParam Integer userId) {

        PracticeStatsResponse stats = practiceService.getPracticeStats(practiceType, userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getPracticeOverview(
            @RequestParam PracticeType practiceType,
            @RequestParam Integer userId
    ) {
        return ResponseEntity.ok(
                practiceService.getPracticeOverview(practiceType, userId)
        );
    }

    @PostMapping("/progress")
    public ResponseEntity<?> submitPracticeProgress(@RequestBody PracticeProgressRequest request) {

        try {
            UserXPHistoryResponse xpResponse = practiceProgressService.processUserAnswer(request);

            Map<String, Object> response = new HashMap<>();

            if (xpResponse.getXp() == 0) {
                response.put("message", "Câu trả lời đã được lưu (không có XP).");
            } else {
                response.put("message", "Trả lời chính xác! Bạn được cộng XP.");
            }

            response.put("data", xpResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "error", e.getMessage()
            ));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", e.getMessage()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "error", "Internal Server Error"
            ));
        }
    }

}
