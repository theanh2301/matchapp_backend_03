package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Practice;
import com.company.mathapp_backend_03.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, Integer> {
    Integer countByPracticeType(PracticeType practiceType);

    @Query("""
    SELECT 
        p.id AS id, 
        p.title AS title, 
        p.description AS description, 
        p.timeLimit AS timeLimit, 
        p.practiceType AS practiceType,
        COUNT(q.id) AS totalQuestions, 
        COALESCE(SUM(q.xpReward), 0) AS totalXp
    FROM Practice p
    LEFT JOIN PracticeQuestion q 
        ON q.practice.id = p.id
    WHERE p.practiceType = :practiceType
    GROUP BY p.id, p.title, p.description, p.timeLimit, p.practiceType
""")
    List<PracticeOverviewDTO> getPracticeSummariesByDifficulty(@Param("practiceType") PracticeType practiceType);

}