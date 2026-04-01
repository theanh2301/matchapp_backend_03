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
        
        -- Tổng số lesson
        (SELECT COUNT(l.id) 
         FROM lessons l 
         JOIN chapters c ON l.chapter_id = c.id 
         WHERE c.subject_id = s.id) AS totalLessons,
         
        -- Số lesson đã hoàn thành
        (SELECT COUNT(lc.id) 
         FROM lesson_completion lc 
         JOIN lessons l ON lc.lesson_id = l.id 
         JOIN chapters c ON l.chapter_id = c.id 
         WHERE c.subject_id = s.id 
           AND lc.user_id = :userId 
           AND lc.is_completed = true) AS completedLessons,
           
        -- Tổng XP từ lesson_completion
        COALESCE((
            SELECT SUM(lc.total_xp)
            FROM lesson_completion lc
            JOIN lessons l ON lc.lesson_id = l.id
            JOIN chapters c ON l.chapter_id = c.id
            WHERE c.subject_id = s.id
              AND lc.user_id = :userId
        ), 0) AS totalXp
        
    FROM subjects s
    WHERE s.subject_class = :subjectClass
    """, nativeQuery = true)
    List<SubjectOverviewDTO> getSubjectOverviewsByClass(
            @Param("userId") Integer userId,
            @Param("subjectClass") Integer subjectClass
    );
}
