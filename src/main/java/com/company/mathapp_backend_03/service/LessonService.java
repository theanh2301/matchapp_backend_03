package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.request.LessonRequest;
import com.company.mathapp_backend_03.model.response.LessonResponse;
import com.company.mathapp_backend_03.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final FlashcardRepository flashcardRepository;
    private final MatchGameRepository matchGameRepository;
    private final QuestionRepository questionRepository;

    public List<LessonResponse> getLessonsByChapterId(Integer id) {
        List<Lesson> lessons = lessonRepository.findByChapterId(id);

        return lessons.stream().map(l -> new LessonResponse(
                l.getId(),
                l.getLessonName(),
                l.getDescription()
        )).toList();
    }

    public void addLesson(LessonRequest lessonRequest) {

        Chapter chapter = chapterRepository.findById(lessonRequest.getChapterId())
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        Optional<Lesson> existingLesson = lessonRepository.findByLessonNameAndChapter(
                    lessonRequest.getLessonName(),
                    chapter
                );

        if (existingLesson.isPresent()) {
            throw new BadRequestException("Lesson already exists in this chapter");
        }

        Lesson lesson = Lesson.builder()
                .lessonName(lessonRequest.getLessonName())
                .description(lessonRequest.getDescription())
                .chapter(chapter)
                .build();
        lessonRepository.save(lesson);
    }

    public void updateLesson(Integer id, LessonRequest lessonRequest) {

        Chapter chapter = chapterRepository.findById(lessonRequest.getChapterId())
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        Optional<Lesson> existingLesson = lessonRepository.findByLessonNameAndChapter(
                lessonRequest.getLessonName(),
                chapter
        );

        if (existingLesson.isPresent()) {
            throw new BadRequestException("Lesson already exists in this chapter");
        }

        lesson.setLessonName(lessonRequest.getLessonName());
        lesson.setDescription(lessonRequest.getDescription());

        lessonRepository.save(lesson);
    }

    public void deleteLesson(Integer id) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        if (flashcardRepository.existsByLessonId(id)
            || matchGameRepository.existsByLessonId(id)
            || questionRepository.existsByLessonId(id)
        ) {
            throw new BadRequestException("Cannot delete lesson because it contains question");
        }

        lessonRepository.delete(lesson);
    }
}
