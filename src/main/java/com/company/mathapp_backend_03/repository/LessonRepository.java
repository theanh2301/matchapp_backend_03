package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    boolean existsByChapterId(Integer id);

    List<Lesson> findByChapterId(Integer chapterId);

    Optional<Lesson> findByLessonNameAndChapter(String lessonName, Chapter chapter);

    boolean existsByLessonNameAndChapterAndIdNot(String lessonName, Chapter chapter, Integer id);
}
