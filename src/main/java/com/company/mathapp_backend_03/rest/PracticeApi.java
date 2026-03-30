package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_03.service.PracticeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/practices")
@AllArgsConstructor
public class PracticeApi {

    private final PracticeService practiceService;

    @GetMapping("/stats")
    public ResponseEntity<PracticeStatsResponse> getStatsByType(
            @RequestParam PracticeType practiceType,
            @RequestParam Integer userId) {

        PracticeStatsResponse stats = practiceService.getPracticeStats(practiceType, userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<PracticeOverviewDTO>>> getPracticeCards(
            @RequestParam PracticeType practiceType) {

        List<PracticeOverviewDTO> cards = practiceService.getPracticeOverview(practiceType);

        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", cards));
    }
}
