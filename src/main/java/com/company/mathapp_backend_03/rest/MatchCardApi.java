package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.request.MatchCardRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.MatchCardResponse;
import com.company.mathapp_backend_03.service.MatchCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match_cards")
@RequiredArgsConstructor
public class MatchCardApi {
    private final MatchCardService matchCardService;

    @GetMapping("/{lessonId}")
    public  ResponseEntity<ApiResponse<List<MatchCardResponse>>> getMatchCard(@PathVariable Integer lessonId) {

        List<MatchCardResponse> matchCard = matchCardService.getMatchCard(lessonId);

        ApiResponse<List<MatchCardResponse>> response = new ApiResponse<>(
                200,
                "Get set match cards successfully",
                matchCard
        );

        return  ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addMatchCard(@Valid @RequestBody MatchCardRequest matchCardRequest) {
        matchCardService.addMatchCard(matchCardRequest);

        return ResponseEntity.ok("MatchCard created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMatchCard(@PathVariable Integer id,
                                            @Valid @RequestBody MatchCardRequest matchCardRequest) {
        matchCardService.updateMatchCard(id,matchCardRequest);
        return ResponseEntity.ok("MatchCard updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMatchCard(@PathVariable Integer id) {
        matchCardService.deleteMatchCard(id);
        return ResponseEntity.ok("MatchCard deleted successfully");
    }
}
