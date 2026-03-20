package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.ConflictException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.LessonRequest;
import com.company.mathapp_backend_03.model.response.LessonResponse;
import com.company.mathapp_backend_03.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final FlashcardRepository flashcardRepository;
    private final MatchCardRepository matchCardRepository;
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
                .orElseThrow(() -> new NotFoundException("Chapter not found"));

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        if (lessonRepository.existsByLessonNameAndChapterAndIdNot(
                lessonRequest.getLessonName(), chapter, id)) {
            throw new ConflictException("Lesson already exists in this chapter");
        }

        if (lesson.getLessonName().equals(lessonRequest.getLessonName())
                && lesson.getDescription().equals(lessonRequest.getDescription())
                && lesson.getChapter().getId().equals(chapter.getId())) {
            throw new BadRequestException("No changes detected");
        }

        lesson.setLessonName(lessonRequest.getLessonName());
        lesson.setDescription(lessonRequest.getDescription());
        lesson.setChapter(chapter);

        lessonRepository.save(lesson);
    }

    public void deleteLesson(Integer id) {

        if (!lessonRepository.existsById(id)) {
            throw new NotFoundException("Lesson not found");
        }

        if (flashcardRepository.existsByLessonId(id)
                || matchCardRepository.existsByLessonId(id)
                || questionRepository.existsByLessonId(id)) {
            throw new ConflictException("Cannot delete lesson because it contains related data");
        }

        try {
            lessonRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete lesson due to data constraints");
        }
    }
}
