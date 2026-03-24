package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Subject;
import com.company.mathapp_backend_03.model.dto.ChapterOverviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    List<Chapter> findChapterBySubjectId(Integer subjectId);

    Optional<Chapter> findByChapterNameAndSubject(String chapterName, Subject subject);

    boolean existsBySubjectId(Integer id);

    boolean existsByChapterNameAndSubjectAndIdNot(String chapterName, Subject subject, Integer id);

    @Query(value = """
        SELECT 
            c.id AS chapterId,
            c.chapter_name AS chapterName,
            c.description AS description,
            
            -- Đếm tổng số Lesson trong Chapter
            (SELECT COUNT(l.id) FROM lessons l WHERE l.chapter_id = c.id) AS totalLessons,
            
            -- Đếm số Lesson user đã hoàn thành trong Chapter
            (SELECT COUNT(lc.id) 
             FROM lesson_completion lc 
             JOIN lessons l ON lc.lesson_id = l.id 
             WHERE l.chapter_id = c.id 
               AND lc.user_id = :userId 
               AND lc.status = 'COMPLETED') AS completedLessons,
               
            -- TÍNH XP ĐÃ ĐẠT ĐƯỢC (EARNED XP)
            (
                COALESCE((SELECT SUM(fp.totalxp) FROM flashcard_progress fp JOIN flashcards f ON fp.flashcard_id = f.id JOIN lessons l ON f.lesson_id = l.id WHERE l.chapter_id = c.id AND fp.user_id = :userId), 0) +
                COALESCE((SELECT SUM(ua.totalxp) FROM user_answer ua JOIN questions q ON ua.question_id = q.id JOIN lessons l ON q.lesson_id = l.id WHERE l.chapter_id = c.id AND ua.user_id = :userId), 0) +
                COALESCE((SELECT SUM(mcr.totalxp) FROM match_card_result mcr JOIN match_card mc ON mcr.match_card_id = mc.id JOIN lessons l ON mc.lesson_id = l.id WHERE l.chapter_id = c.id AND mcr.user_id = :userId), 0)
            ) AS earnedXp,
            
            -- TÍNH TỔNG XP TỐI ĐA CỦA CHAPTER (TOTAL POSSIBLE XP)
            (
                COALESCE((SELECT SUM(f.xp_reward) FROM flashcards f JOIN lessons l ON f.lesson_id = l.id WHERE l.chapter_id = c.id), 0) +
                COALESCE((SELECT SUM(q.xp_reward) FROM questions q JOIN lessons l ON q.lesson_id = l.id WHERE l.chapter_id = c.id), 0) +
                COALESCE((SELECT SUM(mc.xp_reward) FROM match_card mc JOIN lessons l ON mc.lesson_id = l.id WHERE l.chapter_id = c.id), 0)
            ) AS totalPossibleXp
            
        FROM chapters c
        WHERE c.subject_id = :subjectId
        """, nativeQuery = true)
    List<ChapterOverviewDTO> getChapterOverviewsBySubject(@Param("subjectId") Integer subjectId, @Param("userId") Integer userId);
}
