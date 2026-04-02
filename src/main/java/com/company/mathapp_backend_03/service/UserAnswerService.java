package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.QuizAnswerRequest;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository historyRepository;
    private final UserStatRepository userStatRepository;
    private final UserXPHistoryRepository userXPHistoryRepository;

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
    public void processQuizAnswerBatch(List<UserAnswerRequest> requests) {

        if (requests == null || requests.isEmpty()) return;

        // ===== 0. REMOVE DUPLICATE QUESTION =====
        requests = requests.stream()
                .collect(Collectors.toMap(
                        UserAnswerRequest::getQuestionId,
                        r -> r,
                        (oldVal, newVal) -> newVal
                ))
                .values()
                .stream()
                .toList();

        // ===== 1. USER =====
        Integer userId = requests.get(0).getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // ===== 2. LOAD QUESTIONS =====
        List<Integer> questionIds = requests.stream()
                .map(UserAnswerRequest::getQuestionId)
                .toList();

        Map<Integer, QuizQuestion> questionMap =
                quizQuestionRepository.findAllById(questionIds)
                        .stream()
                        .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        // ===== 3. LOAD ANSWERS (QUAN TRỌNG) =====
        List<Integer> answerIds = requests.stream()
                .map(UserAnswerRequest::getAnswerId)
                .toList();

        Map<Integer, QuizAnswer> answerMap =
                quizAnswerRepository.findAllById(answerIds)
                        .stream()
                        .collect(Collectors.toMap(QuizAnswer::getId, a -> a));

        // ===== 4. LOAD PROGRESS =====
        List<UserAnswer> existing =
                userAnswerRepository.findByUserIdAndQuizQuestionIdIn(userId, questionIds);

        Map<Integer, UserAnswer> progressMap = existing.stream()
                .collect(Collectors.toMap(p -> p.getQuizQuestion().getId(), p -> p));

        // ===== 5. LOAD HISTORY =====
        List<UserXPHistory> historyList =
                userXPHistoryRepository.findByUserIdAndSourcedIdInAndSource(
                        userId,
                        questionIds,
                        Source.QUIZ_GAME
                );

        Set<Integer> existingHistoryIds = historyList.stream()
                .map(UserXPHistory::getSourcedId)
                .collect(Collectors.toSet());

        // ===== 6. PREPARE =====
        List<UserAnswer> progressToSave = new ArrayList<>();
        List<UserXPHistory> historyToSave = new ArrayList<>();

        int totalXpGained = 0;

        // ===== 7. LOOP =====
        for (UserAnswerRequest request : requests) {

            QuizQuestion question = questionMap.get(request.getQuestionId());
            if (question == null) continue;

            QuizAnswer answer = answerMap.get(request.getAnswerId());
            if (answer == null) {
                throw new EntityNotFoundException("Answer not found");
            }

            // ❗ CHECK answer thuộc question
            if (!answer.getQuizQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Answer does not belong to question");
            }

            // ❗ BACKEND tự check đúng sai
            boolean isCorrect = Boolean.TRUE.equals(answer.getIsCorrect());

            UserAnswer progress = progressMap.get(question.getId());

            boolean alreadyCorrect = progress != null && Boolean.TRUE.equals(progress.getIsCorrect());

            int earnedXp = (isCorrect && !alreadyCorrect)
                    ? question.getXpReward()
                    : 0;

            // ===== CREATE / UPDATE =====
            if (progress == null) {
                progress = new UserAnswer();
                progress.setUser(user);
                progress.setQuizQuestion(question);
            }

            progress.setIsCorrect(isCorrect);
            progress.setAnsweredAt(request.getAnsweredAt());
            progress.setQuizAnswer(answer);

            progress.setTotalXP(
                    (progress.getTotalXP() == null ? 0 : progress.getTotalXP()) + earnedXp
            );

            progressToSave.add(progress);

            // ===== XP HISTORY =====
            if (earnedXp > 0 && !existingHistoryIds.contains(question.getId())) {

                UserXPHistory history = new UserXPHistory();
                history.setUser(user);
                history.setXp(earnedXp);
                history.setSource(Source.QUIZ_GAME);
                history.setSourcedId(question.getId());

                historyToSave.add(history);

                totalXpGained += earnedXp;
            }
        }

        // ===== 8. SAVE =====
        userAnswerRepository.saveAll(progressToSave);

        if (!historyToSave.isEmpty()) {
            userXPHistoryRepository.saveAll(historyToSave);
        }

        // ===== 9. UPDATE USER =====
        if (totalXpGained > 0) {
            updateUserStats(user, totalXpGained);
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
