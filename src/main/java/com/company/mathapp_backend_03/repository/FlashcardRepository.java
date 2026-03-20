package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Flashcard;
import com.company.mathapp_backend_03.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
    boolean existsByLessonId(Integer id);

    List<Flashcard> findByLessonId(Integer id);

    Optional<Flashcard> findByFrontTextAndBackTextAndLesson(String frontText, String backText, Lesson lesson);

    boolean existsByFrontTextAndBackTextAndLessonAndIdNot(String trim, String trim1, Lesson lesson, Integer id);
}
