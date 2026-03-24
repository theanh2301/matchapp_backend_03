package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Answer;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.entity.UserAnswer;
import com.company.mathapp_backend_03.model.request.UserAnswerRequest;
import com.company.mathapp_backend_03.model.response.UserAnswerResponse;
import com.company.mathapp_backend_03.repository.AnswerRepository;
import com.company.mathapp_backend_03.repository.QuestionRepository;
import com.company.mathapp_backend_03.repository.UserAnswerRepository;
import com.company.mathapp_backend_03.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public UserAnswerResponse getUserAnswer(Integer userId, Integer questionId) {
        Optional<UserAnswer> userAnswerOpt = userAnswerRepository.findByUserIdAndQuestionId(userId, questionId);

        if (userAnswerOpt.isEmpty()) {
            return null;
        }

        UserAnswer userAnswer = userAnswerOpt.get();
        return new UserAnswerResponse(
                userAnswer.getId(),
                userAnswer.getIsCorrect(),
                userAnswer.getAnsweredAt(),
                userAnswer.getTotalXP()
        );
    }

    @Transactional
    public void addOrUpdateUserAnswer(UserAnswerRequest userAnswerRequest) {

        Question question = questionRepository.findById(userAnswerRequest.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        Answer answer = answerRepository.findById(userAnswerRequest.getAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

        User user = userRepository.findById(userAnswerRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!answer.getQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("This answer does not belong to the requested question.");
        }

        UserAnswer userAnswer = userAnswerRepository
                .findByUserAndQuestion(user, question)
                .orElseGet(() -> {
                    UserAnswer newUserAnswer = new UserAnswer();
                    newUserAnswer.setUser(user);
                    newUserAnswer.setQuestion(question);
                    return newUserAnswer;
                });

        userAnswer.setAnswer(answer);

        boolean isCorrect = answer.getIsCorrect();
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setAnsweredAt(LocalDateTime.now());

        if (isCorrect) {
            userAnswer.setTotalXP(question.getXpReward());
        } else {
            userAnswer.setTotalXP(0);
        }

        try {
            userAnswerRepository.save(userAnswer);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Operation too fast, your progress is being processed.");
        }
    }
}
