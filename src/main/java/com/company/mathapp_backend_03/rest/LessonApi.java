package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.dto.LessonOverviewDTO;
import com.company.mathapp_backend_03.model.dto.SuggestedLessonDTO;
import com.company.mathapp_backend_03.model.request.LessonRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.LessonResponse;
import com.company.mathapp_backend_03.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/lessons")
@RestController
@RequiredArgsConstructor
public class LessonApi {
    private final LessonService lessonService;

    @GetMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<List<LessonOverviewDTO>>> getLessonsInChapter(
            @RequestParam Integer userId,
            @PathVariable Integer chapterId
            ) {

        List<LessonOverviewDTO> overviews  = lessonService.getLessonOverviewsByChapterId(userId, chapterId);

        ApiResponse<List<LessonOverviewDTO>> response = new ApiResponse<>(
                200,
                "Lấy danh sách bài học thành công",
                overviews
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggested-lessons")
    public ResponseEntity<Map<String, Object>> getSuggestedLessons(
            @RequestParam Integer userId,
            @RequestParam Integer subjectId
    ) {

        List<SuggestedLessonDTO> lessons = lessonService.getSuggestedLessons(userId, subjectId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lấy danh sách lesson gợi ý thành công");
        response.put("data", lessons);

        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public List<LessonResponse> getLessons(@RequestParam Integer chapterId) {
        return lessonService.getLessonsByChapterId(chapterId);
    }

    @PostMapping
    public ResponseEntity<?> addLesson(@Valid @RequestBody LessonRequest lessonRequest) {
        lessonService.addLesson(lessonRequest);
        return ResponseEntity.ok("Lesson created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable Integer id,
                                         @Valid @RequestBody LessonRequest lessonRequest) {

        lessonService.updateLesson(id, lessonRequest);

        return ResponseEntity.ok("Lesson updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer id) {
        lessonService.deleteLesson(id);

        return ResponseEntity.ok("Lesson deleted successfully");
    }
}
