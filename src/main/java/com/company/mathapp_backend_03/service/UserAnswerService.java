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

        QuizQuestion question = quizQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Question"));

        QuizAnswer answer = quizAnswerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Answer"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        // validate
        if (!answer.getQuizQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("Answer không thuộc Question");
        }

        // ⚠️ nên dùng lock nếu có thể
        UserAnswer existing = userAnswerRepository
                .findByUserAndQuizQuestion(user, question)
                .orElse(null);

        boolean isCorrect = Boolean.TRUE.equals(answer.getIsCorrect());

        // ❗ nếu đã đúng trước đó → block luôn
        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
            return buildNoXpResponse(user.getId(), question.getId());
        }

        // ✅ xác định lần đầu đúng
        boolean isFirstTimeCorrect = isCorrect &&
                (existing == null || !Boolean.TRUE.equals(existing.getIsCorrect()));

        int earnedXp = isFirstTimeCorrect ? question.getXpReward() : 0;

        try {
            // 🔥 update answer TRƯỚC
            UserAnswer saved = saveOrUpdateUserAnswer(user, question, answer, existing, isCorrect, earnedXp);

            UserXPHistory history = null;

            // 🔥 chỉ cộng XP khi first-time correct
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, question.getId());
                updateUserStats(user, earnedXp);
            }

            return buildResponse(history, earnedXp, user.getId(), question.getId());

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Spam click detected, thử lại");
        }
    }

    private UserAnswer saveOrUpdateUserAnswer(User user,
                                              QuizQuestion question,
                                              QuizAnswer answer,
                                              UserAnswer existing,
                                              boolean isCorrect,
                                              int earnedXp) {

        // ❗ nếu đã đúng → không update
        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
            return existing;
        }

        UserAnswer entity = (existing != null) ? existing : new UserAnswer();

        entity.setUser(user);
        entity.setQuizQuestion(question);
        entity.setQuizAnswer(answer);

        entity.setAnsweredAt(LocalDateTime.now());
        entity.setIsCorrect(isCorrect);

        // 🔥 CHỈ set XP khi được cộng
        if (earnedXp > 0) {
            entity.setTotalXP(earnedXp);
        } else if (existing == null) {
            entity.setTotalXP(0);
        }
        // ❗ KHÔNG reset về 0 nếu đã từng đúng

        return userAnswerRepository.save(entity);
    }

    private UserXPHistory addXpHistory(User user, int xp, int questionId) {
        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.QUIZ_GAME);
        history.setSourcedId(questionId);
        history.setEarnedAt(LocalDateTime.now());

        return historyRepository.save(history);
    }

    private void updateUserStats(User user, int earnedXp) {

        UserStat stats = userStatRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserStat s = new UserStat();
                    s.setUser(user);
                    s.setTotalXP(0);
                    s.setTotalLesson(0);
                    s.setStreakDay(0);
                    s.setLastStudyDate(LocalDateTime.now());
                    return s;
                });

        stats.setTotalXP((stats.getTotalXP() == null ? 0 : stats.getTotalXP()) + earnedXp);

        userStatRepository.save(stats);
    }

    private UserXPHistoryResponse buildResponse(UserXPHistory history,
                                                int earnedXp,
                                                Integer userId,
                                                Integer questionId) {

        UserXPHistoryResponse res = new UserXPHistoryResponse();

        res.setUserId(userId);
        res.setXp(earnedXp);
        res.setSource("QUIZ_GAME");
        res.setSourceId(questionId);
        res.setEarnedAt(LocalDateTime.now());

        if (history != null) {
            res.setId(history.getId());
        }

        return res;
    }

    private UserXPHistoryResponse buildNoXpResponse(Integer userId, Integer questionId) {

        UserXPHistoryResponse res = new UserXPHistoryResponse();

        res.setUserId(userId);
        res.setXp(0);
        res.setSource("QUIZ_GAME");
        res.setSourceId(questionId);
        res.setEarnedAt(LocalDateTime.now());

        return res;
    }
}
