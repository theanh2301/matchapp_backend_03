package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Answer;
import com.company.mathapp_backend_03.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    boolean existsByQuestionId(Integer id);

    List<Answer> findAnswerByQuestionId(Integer questionId);

    List<Answer> findByQuestion(Question question);

    void deleteByQuestion(Question question);

    @Modifying
    @Query("DELETE FROM Answer a WHERE a.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Integer questionId);

}
