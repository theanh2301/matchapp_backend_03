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
        s.subject_class AS subjectClass,
        s.subject_name AS subjectName,
        s.icon AS icon,

        COUNT(DISTINCT l.id) AS totalLessons,

        COUNT(DISTINCT CASE 
            WHEN lc.is_completed = true THEN l.id 
        END) AS completedLessons,

        COALESCE(SUM(lc.total_xp), 0) AS earnedXp,

        COALESCE(SUM(
            (
                SELECT COALESCE(SUM(f.xp_reward),0)
                FROM flashcards f WHERE f.lesson_id = l.id
            ) +
            (
                SELECT COALESCE(SUM(m.xp_reward),0)
                FROM match_card m WHERE m.lesson_id = l.id
            ) +
            (
                SELECT COALESCE(SUM(q.xp_reward),0)
                FROM quiz_questions q WHERE q.lesson_id = l.id
            )
        ),0) AS totalXp

    FROM subjects s

    LEFT JOIN chapters c ON c.subject_id = s.id
    LEFT JOIN lessons l ON l.chapter_id = c.id

    LEFT JOIN lesson_completion lc 
        ON lc.lesson_id = l.id 
        AND lc.user_id = :userId

    WHERE s.subject_class = :subjectClass

    GROUP BY s.id, s.subject_class, s.subject_name, s.icon
""", nativeQuery = true)
    List<SubjectOverviewDTO> getSubjectOverviewsByClass(
            @Param("userId") Integer userId,
            @Param("subjectClass") Integer subjectClass
    );
}
