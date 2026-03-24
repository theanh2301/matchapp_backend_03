package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.dto.ChapterOverviewDTO;
import com.company.mathapp_backend_03.model.request.ChapterRequest;
import com.company.mathapp_backend_03.model.response.ChapterResponse;
import com.company.mathapp_backend_03.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/chapters")
@RestController
@RequiredArgsConstructor
public class ChapterApi {
    private final ChapterService chapterService;

    @GetMapping("/{subjectId}/chapters")
    public ResponseEntity<List<ChapterOverviewDTO>> getChaptersInSubject(
            @PathVariable Integer subjectId,
            @RequestParam Integer userId) {

        List<ChapterOverviewDTO> chapters = chapterService.getChaptersBySubject(subjectId, userId);
        return ResponseEntity.ok(chapters);
    }

    @GetMapping("/{subjectId}")
    public List<ChapterResponse> getChapters(@PathVariable Integer subjectId) {

        return chapterService.getChaptersBySubjectId(subjectId);
    }

    @PostMapping
    public ResponseEntity<?> addChapter(@Valid @RequestBody ChapterRequest chapterRequest) {
        chapterService.addChapter(chapterRequest);
        return ResponseEntity.ok("Chapter created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChapter(@PathVariable Integer id,
                                          @Valid @RequestBody ChapterRequest chapterRequest) {

        chapterService.updateChapter(id, chapterRequest);

        return ResponseEntity.ok("Chapter updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Integer id) {

        chapterService.deleteChapter(id);

        return ResponseEntity.ok("Chapter deleted successfully");
    }
}
