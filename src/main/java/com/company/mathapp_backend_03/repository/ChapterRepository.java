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
        
        -- Tổng số lesson
        (SELECT COUNT(l.id) 
         FROM lessons l 
         WHERE l.chapter_id = c.id) AS totalLessons,
        
        -- Số lesson đã hoàn thành
        (SELECT COUNT(lc.id) 
         FROM lesson_completion lc 
         JOIN lessons l ON lc.lesson_id = l.id 
         WHERE l.chapter_id = c.id 
           AND lc.user_id = :userId 
           AND lc.is_completed = true) AS completedLessons,
           
        -- XP đã đạt (từ lesson_completion)
        COALESCE((
            SELECT SUM(lc.total_xp)
            FROM lesson_completion lc
            JOIN lessons l ON lc.lesson_id = l.id
            WHERE l.chapter_id = c.id
              AND lc.user_id = :userId
        ), 0) AS earnedXp,
        
        -- Tổng XP tối đa (giữ nguyên)
        (
            COALESCE((SELECT SUM(f.xp_reward) 
                      FROM flashcards f 
                      JOIN lessons l ON f.lesson_id = l.id 
                      WHERE l.chapter_id = c.id), 0) +
            COALESCE((SELECT SUM(q.xp_reward) 
                      FROM quiz_questions q 
                      JOIN lessons l ON q.lesson_id = l.id 
                      WHERE l.chapter_id = c.id), 0) +
            COALESCE((SELECT SUM(mc.xp_reward) 
                      FROM match_card mc 
                      JOIN lessons l ON mc.lesson_id = l.id 
                      WHERE l.chapter_id = c.id), 0)
        ) AS totalPossibleXp
        
    FROM chapters c
    WHERE c.subject_id = :subjectId
    """, nativeQuery = true)
    List<ChapterOverviewDTO> getChapterOverviewsBySubject(
            @Param("userId") Integer userId,
            @Param("subjectId") Integer subjectId
    );
}
