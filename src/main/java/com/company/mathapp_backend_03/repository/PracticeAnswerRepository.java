package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.PracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeAnswerRepository extends JpaRepository<PracticeAnswer, Integer> {
    List<PracticeAnswer> findByPracticeQuestionId(Integer id);
}
