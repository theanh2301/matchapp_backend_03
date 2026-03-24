package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.model.dto.LessonOverviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
            
            -- TÍNH XP ĐÃ ĐẠT ĐƯỢC CỦA BÀI HỌC
            (
                COALESCE((SELECT SUM(fp.totalxp) FROM flashcard_progress fp JOIN flashcards f ON fp.flashcard_id = f.id WHERE f.lesson_id = l.id AND fp.user_id = :userId), 0) +
                COALESCE((SELECT SUM(ua.totalxp) FROM user_answer ua JOIN questions q ON ua.question_id = q.id WHERE q.lesson_id = l.id AND ua.user_id = :userId), 0) +
                COALESCE((SELECT SUM(mcr.totalxp) FROM match_card_result mcr JOIN match_card mc ON mcr.match_card_id = mc.id WHERE mc.lesson_id = l.id AND mcr.user_id = :userId), 0)
            ) AS earnedXp,
            
            -- TÍNH TỔNG XP TỐI ĐA CỦA BÀI HỌC
            (
                COALESCE((SELECT SUM(f.xp_reward) FROM flashcards f WHERE f.lesson_id = l.id), 0) +
                COALESCE((SELECT SUM(q.xp_reward) FROM questions q WHERE q.lesson_id = l.id), 0) +
                COALESCE((SELECT SUM(mc.xp_reward) FROM match_card mc WHERE mc.lesson_id = l.id), 0)
            ) AS totalPossibleXp,
            
            -- TRẠNG THÁI HOÀN THÀNH TỪNG GAME (1 nếu user có dữ liệu điểm, 0 nếu không)
            CASE WHEN (SELECT COUNT(fp.id) FROM flashcard_progress fp JOIN flashcards f ON fp.flashcard_id = f.id WHERE f.lesson_id = l.id AND fp.user_id = :userId) > 0 THEN 1 ELSE 0 END AS isFlashcardDone,
            CASE WHEN (SELECT COUNT(ua.id) FROM user_answer ua JOIN questions q ON ua.question_id = q.id WHERE q.lesson_id = l.id AND ua.user_id = :userId) > 0 THEN 1 ELSE 0 END AS isQuestionDone,
            CASE WHEN (SELECT COUNT(mcr.id) FROM match_card_result mcr JOIN match_card mc ON mcr.match_card_id = mc.id WHERE mc.lesson_id = l.id AND mcr.user_id = :userId) > 0 THEN 1 ELSE 0 END AS isMatchCardDone
            
        FROM lessons l
        WHERE l.chapter_id = :chapterId
        """, nativeQuery = true)
    List<LessonOverviewDTO> getLessonOverviewsByChapter(@Param("chapterId") Integer chapterId, @Param("userId") Integer userId);
}
