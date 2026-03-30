package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.PracticeQuestion;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, Integer> {
    List<PracticeQuestion> findByPracticeIdAndDifficulty(Integer practiceId, Difficulty difficulty);

    List<PracticeQuestion> findByPracticeId(Integer id);
}
