package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Answer;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Integer> {
    Optional<UserAnswer> findByUserAndQuestion(User user, Question question);

    Optional<UserAnswer> findByUserIdAndQuestionId(Integer userId, Integer questionId);
}
