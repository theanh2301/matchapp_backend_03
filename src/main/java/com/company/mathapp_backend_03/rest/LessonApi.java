package com.company.mathapp_backend_03.rest;
import com.company.mathapp_backend_03.model.request.LessonRequest;
import com.company.mathapp_backend_03.model.response.LessonResponse;
import com.company.mathapp_backend_03.service.ChapterService;
import com.company.mathapp_backend_03.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/lessons")
@RestController
@RequiredArgsConstructor
public class LessonApi {
    private final LessonService lessonService;
    private final ChapterService chapterService;

    @GetMapping("/{chapterId}")
    public List<LessonResponse> getLessons(@PathVariable Integer chapterId) {
        return lessonService.getLessonsByChapterId(chapterId);
    }

    @PostMapping
    public ResponseEntity<?> addLesson(@RequestBody LessonRequest lessonRequest) {
        lessonService.addLesson(lessonRequest);
        return ResponseEntity.ok("Lesson created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLesson(@PathVariable Integer id,
                                          @RequestBody LessonRequest lessonRequest) {

        lessonService.updateLesson(id, lessonRequest);

        return ResponseEntity.ok("Lesson updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer id) {
        lessonService.deleteLesson(id);

        return ResponseEntity.ok("Lesson deleted successfully");
    }
}
