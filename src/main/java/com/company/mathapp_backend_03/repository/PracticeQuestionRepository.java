package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Practice;
import com.company.mathapp_backend_03.entity.PracticeQuestion;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    List<PracticeQuestion> findByPracticeIdAndDifficulty(Integer practiceId, Difficulty difficulty);

    List<PracticeQuestion> findByPracticeId(Integer id);

    boolean existsByContentAndPracticeAndIdNot(String trim, Practice practice, Integer id);

    Optional<PracticeQuestion> findByContentAndPractice(String content, Practice practice);

    @Query(value = """
    SELECT q.*
    FROM practice_questions q
    JOIN (
        SELECT 
            pp.practice_question_id,
            MAX(pp.answered_at) AS latest_time
        FROM practice_progress pp
        JOIN practice_questions pq 
            ON pq.id = pp.practice_question_id
        WHERE pq.practice_id = :practiceId
          AND pp.user_id = :userId
        GROUP BY pp.practice_question_id
    ) latest 
        ON latest.practice_question_id = q.id

    JOIN practice_progress pp 
        ON pp.practice_question_id = latest.practice_question_id
        AND pp.answered_at = latest.latest_time

    WHERE pp.is_correct = 0
""", nativeQuery = true)
    List<PracticeQuestion> findWrongQuestions(
            @Param("practiceId") Integer practiceId,
            @Param("userId") Integer userId
    );
}
