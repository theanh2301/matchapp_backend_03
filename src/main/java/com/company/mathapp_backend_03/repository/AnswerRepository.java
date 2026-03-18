package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    boolean existsByQuestionId(Integer id);
}
