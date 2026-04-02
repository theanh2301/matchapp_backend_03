package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.model.dto.LessonOverviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    boolean existsByChapterId(Integer id);

    List<Lesson> findByChapterId(Integer chapterId);

    Optional<Lesson> findByLessonNameAndChapter(String lessonName, Chapter chapter);

    boolean existsByLessonNameAndChapterAndIdNot(String lessonName, Chapter chapter, Integer id);

    @Query(value = """
    SELECT 
        l.id AS lessonId,
        l.lesson_name AS lessonName,
        l.description AS description,

        -- XP user đã đạt
        COALESCE(lc.total_xp, 0) AS earnedXp,

        -- Tổng XP tối đa
        (
            COALESCE((SELECT SUM(f.xp_reward) FROM flashcards f WHERE f.lesson_id = l.id),0)
            +
            COALESCE((SELECT SUM(m.xp_reward) FROM match_card m WHERE m.lesson_id = l.id),0)
            +
            COALESCE((SELECT SUM(q.xp_reward) FROM quiz_questions q WHERE q.lesson_id = l.id),0)
        ) AS totalPossibleXp,

        -- Trạng thái hoàn thành từng loại
        CASE WHEN lc.is_flashcard_completed = TRUE THEN 1 ELSE 0 END AS isFlashcardDone,
        CASE WHEN lc.is_quiz_completed = TRUE THEN 1 ELSE 0 END AS isQuestionDone,
        CASE WHEN lc.is_match_card_completed = TRUE THEN 1 ELSE 0 END AS isMatchCardDone

    FROM lessons l

    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId

    WHERE l.chapter_id = :chapterId
""", nativeQuery = true)
    List<LessonOverviewDTO> getLessonOverview(
            @Param("userId") Integer userId,
            @Param("chapterId") Integer chapterId
    );
}
