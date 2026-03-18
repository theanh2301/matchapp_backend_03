package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    boolean existsByLessonId(Integer id);

    List<Question> findByLessonId(Integer id);

    Optional<Question> findByContentAndTypeQuestionAndLesson(String content, TypeQuestion typeQuestion, Lesson lesson);

    boolean existsByContentAndTypeQuestionAndLessonAndIdNot(String content, TypeQuestion typeQuestion, Lesson lesson, Integer id);
}
