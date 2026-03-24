package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Subject;
import com.company.mathapp_backend_03.model.dto.SubjectOverviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    Optional<Subject> findBySubjectNameIgnoreCaseAndSubjectClass(String subjectName, Integer subjectClass);

    @Query(value = """
    SELECT 
        s.id AS subjectId,
        s.subject_name AS subjectName,
        s.icon AS icon,
        
        -- Đếm tổng số Chapter
        (SELECT COUNT(c.id) FROM chapters c WHERE c.subject_id = s.id) AS totalChapters,
        
        -- Đếm tổng số Lesson
        (SELECT COUNT(l.id) 
         FROM lessons l 
         JOIN chapters c ON l.chapter_id = c.id 
         WHERE c.subject_id = s.id) AS totalLessons,
         
        -- Đếm số Lesson user đã hoàn thành
        (SELECT COUNT(lc.id) 
         FROM lesson_completion lc 
         JOIN lessons l ON lc.lesson_id = l.id 
         JOIN chapters c ON l.chapter_id = c.id 
         WHERE c.subject_id = s.id 
           AND lc.user_id = :userId 
           AND lc.status = 'COMPLETED') AS completedLessons,
           
        -- Tổng XP = XP Flashcard + XP Question + XP MatchCard
        (
            COALESCE((SELECT SUM(fp.totalxp) 
                      FROM flashcard_progress fp 
                      JOIN flashcards f ON fp.flashcard_id = f.id 
                      JOIN lessons l ON f.lesson_id = l.id 
                      JOIN chapters c ON l.chapter_id = c.id 
                      WHERE c.subject_id = s.id AND fp.user_id = :userId), 0) 
            +
            COALESCE((SELECT SUM(ua.totalxp) 
                      FROM user_answer ua 
                      JOIN questions q ON ua.question_id = q.id 
                      JOIN lessons l ON q.lesson_id = l.id 
                      JOIN chapters c ON l.chapter_id = c.id 
                      WHERE c.subject_id = s.id AND ua.user_id = :userId), 0) 
            +
            -- Đã đổi sang bảng match_card_result và match_card theo đúng Entity của bạn
            COALESCE((SELECT SUM(mcr.totalxp) 
                      FROM match_card_result mcr 
                      JOIN match_card mc ON mcr.match_card_id = mc.id 
                      JOIN lessons l ON mc.lesson_id = l.id 
                      JOIN chapters c ON l.chapter_id = c.id 
                      WHERE c.subject_id = s.id AND mcr.user_id = :userId), 0)
        ) AS totalXp
        
    FROM subjects s
    """, nativeQuery = true)

    List<SubjectOverviewDTO> getSubjectOverviews(@Param("userId") Integer userId);

}
