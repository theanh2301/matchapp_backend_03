package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.UserAnswerRequest;
import com.company.mathapp_backend_03.model.response.UserAnswerResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository historyRepository;
    private final UserStatRepository userStatRepository;

    public UserAnswerResponse getUserAnswer(Integer userId, Integer questionId) {
        Optional<UserAnswer> userAnswerOpt = userAnswerRepository.findByUserIdAndQuizQuestionId(userId, questionId);

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

        QuizQuestion quizQuestion = quizQuestionRepository.findById(userAnswerRequest.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        QuizAnswer quizAnswer = quizAnswerRepository.findById(userAnswerRequest.getAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

        User user = userRepository.findById(userAnswerRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!quizAnswer.getQuizQuestion().getId().equals(quizQuestion.getId())) {
            throw new IllegalArgumentException("This answer does not belong to the requested question.");
        }

        UserAnswer userAnswer = userAnswerRepository
                .findByUserAndQuizQuestion(user, quizQuestion)
                .orElseGet(() -> {
                    UserAnswer newUserAnswer = new UserAnswer();
                    newUserAnswer.setUser(user);
                    newUserAnswer.setQuizQuestion(quizQuestion);
                    return newUserAnswer;
                });

        userAnswer.setQuizAnswer(quizAnswer);

        boolean isCorrect = quizAnswer.getIsCorrect();
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setAnsweredAt(LocalDateTime.now());

        if (isCorrect) {
            userAnswer.setTotalXP(quizQuestion.getXpReward());
        } else {
            userAnswer.setTotalXP(0);
        }

        try {
            userAnswerRepository.save(userAnswer);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Operation too fast, your progress is being processed.");
        }
    }

    @Transactional
    public UserXPHistoryResponse processUserAnswer(UserAnswerRequest request) {

        QuizQuestion quizQuestion = quizQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Question"));

        QuizAnswer quizAnswer = quizAnswerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Answer"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        if (!quizAnswer.getQuizQuestion().getId().equals(quizQuestion.getId())) {
            throw new IllegalArgumentException("Câu trả lời này không thuộc về câu hỏi được yêu cầu.");
        }

        UserAnswer existingUserAnswer = userAnswerRepository
                .findByUserAndQuizQuestion(user, quizQuestion)
                .orElse(null);

        boolean isCorrect = quizAnswer.getIsCorrect();
        int earnedXp = 0;

        boolean isFirstTimeCorrect = isCorrect && (existingUserAnswer == null || !existingUserAnswer.getIsCorrect());

        if (isFirstTimeCorrect) {
            earnedXp = quizQuestion.getXpReward();
        }

        try {
            updateUserAnswerRecord(user, quizQuestion, quizAnswer, existingUserAnswer, isCorrect);

            UserXPHistory history = null;
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, quizQuestion.getId());
                updateUserStats(user, earnedXp);
            }

            return history != null ? mapToResponse(history) : null;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Thao tác quá nhanh, tiến độ đang được xử lý.");
        }
    }

    private void updateUserAnswerRecord(User user, QuizQuestion quizQuestion, QuizAnswer quizAnswer, UserAnswer existingRecord, boolean isCorrect) {
        UserAnswer userAnswer = existingRecord != null ? existingRecord : new UserAnswer();

        userAnswer.setUser(user);
        userAnswer.setQuizQuestion(quizQuestion);
        userAnswer.setQuizAnswer(quizAnswer);
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setAnsweredAt(LocalDateTime.now());

        if (existingRecord != null && existingRecord.getIsCorrect() != null && existingRecord.getIsCorrect()) {

        } else {
            userAnswer.setIsCorrect(isCorrect);
            if (isCorrect) {
                userAnswer.setTotalXP(quizQuestion.getXpReward());
            } else {
                userAnswer.setTotalXP(0);
            }
        }

        userAnswerRepository.save(userAnswer);
    }

    private UserXPHistory addXpHistory(User user, int xp, int questionId) {
        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.QUIZ_GAME); // Phân biệt nguồn XP từ làm trắc nghiệm
        history.setSourcedId(questionId);
        history.setEarnedAt(LocalDateTime.now());

        return historyRepository.save(history);
    }

    private void updateUserStats(User user, int earnedXp) {
        UserStat stats = userStatRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserStat newStats = new UserStat();
                    newStats.setUserId(user.getId());
                    newStats.setTotalXP(0);
                    return newStats;
                });

        int currentTotalXp = stats.getTotalXP() != null ? stats.getTotalXP() : 0;
        stats.setTotalXP(currentTotalXp + earnedXp);
        userStatRepository.save(stats);
    }

    private UserXPHistoryResponse mapToResponse(UserXPHistory entity) {
        UserXPHistoryResponse response = new UserXPHistoryResponse();
        response.setId(entity.getId());
        if (entity.getUser() != null) response.setUserId(entity.getUser().getId());
        response.setXp(entity.getXp());
        response.setSource(entity.getSource().name());
        response.setSourceId(entity.getSourcedId());
        response.setEarnedAt(entity.getEarnedAt());
        return response;
    }
}
