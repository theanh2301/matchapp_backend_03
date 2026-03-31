package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.MatchCard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchCardRepository extends JpaRepository<MatchCard, Integer> {
    boolean existsByLessonId(Integer id);

    List<MatchCard> findByLessonId(Integer id);

    List<MatchCard> findByPairIdAndLesson(Integer pairId, Lesson lesson);

    boolean existsByContentAndLesson(String content, Lesson lesson);

    boolean existsByContentAndLessonAndPairIdNot(
            String content,
            Lesson lesson,
            Integer pairId
    );


}