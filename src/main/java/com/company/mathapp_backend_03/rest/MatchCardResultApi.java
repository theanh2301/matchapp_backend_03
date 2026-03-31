package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.MatchCardResultRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.MatchCardResponse;
import com.company.mathapp_backend_03.model.response.MatchCardResultResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.service.MatchCardResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match_card_result")
@RequiredArgsConstructor
public class MatchCardResultApi {
    private final MatchCardResultService matchCardResultService;

    @GetMapping("/{userId}/{matchCardId}")
    public ResponseEntity<MatchCardResultResponse> getMatchCardResult(@PathVariable Integer userId,
                                                            @PathVariable Integer matchCardId) {

        MatchCardResultResponse response = matchCardResultService.getMatchCardResult(userId, matchCardId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveResult(@Valid @RequestBody MatchCardResultRequest matchCardResultRequest) {
        matchCardResultService.addOrUpdateMatchCardResult(matchCardResultRequest);
        return ResponseEntity.ok("Result updated successfully");
    }

    @PostMapping("/progress")
    public ResponseEntity<Map<String, Object>> submitMatchCardResult(@RequestBody MatchCardResultRequest request) {

        UserXPHistoryResponse xpResponse = matchCardResultService.processMatchCardResult(request);

        Map<String, Object> response = new HashMap<>();

        if (xpResponse == null) {
            response.put("message", "Đã lưu kỷ lục. Hãy cố gắng ghép được nhiều cặp hơn để nhận XP nhé!");
            response.put("data", null);
        } else {
            response.put("message", "Tuyệt vời! Bạn vừa phá kỷ lục và nhận được XP.");
            response.put("data", xpResponse);
        }

        return ResponseEntity.ok(response);
    }
}
