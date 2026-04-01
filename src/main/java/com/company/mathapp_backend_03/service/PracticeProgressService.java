package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.PracticeProgressRequest;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PracticeProgressService {

    private final PracticeProgressRepository practiceProgressRepository;
    private final PracticeRepository practiceRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository historyRepository;
    private final UserStatRepository userStatRepository;
    private final UserPracticeRepository userPracticeRepository;

    @Transactional
    public UserXPHistoryResponse processUserAnswer(PracticeProgressRequest request) {

        PracticeQuestion question = practiceQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Question"));

        PracticeAnswer answer = practiceAnswerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Answer"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        // validate
        if (!answer.getPracticeQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("Answer không thuộc Question");
        }

        // ⚠️ nên dùng lock nếu có thể
        PracticeProgress existing = practiceProgressRepository
                .findByUserAndPracticeQuestion(user, question)
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
            // 🔥 1. Lưu progress trước
            PracticeProgress saved = saveOrUpdateUserPractice(
                    user, question, answer, existing, isCorrect, earnedXp
            );

            // 🔥 2. UPDATE USER PRACTICE (ĐẶT Ở ĐÂY)
            updateUserPractice(
                    question.getPractice().getId(),
                    user.getId()
            );

            UserXPHistory history = null;

            // 🔥 3. Cộng XP nếu lần đầu đúng
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, question.getId());
                updateUserStats(user, earnedXp);
            }

            return buildResponse(history, earnedXp, user.getId(), question.getId());

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Spam click detected, thử lại");
        }

    }

    private PracticeProgress saveOrUpdateUserPractice(User user,
                                                          PracticeQuestion question,
                                                          PracticeAnswer answer,
                                                          PracticeProgress existing,
                                                          boolean isCorrect,
                                                          int earnedXp) {

        // ❗ nếu đã đúng → không update
        if (existing != null && Boolean.TRUE.equals(existing.getIsCorrect())) {
            return existing;
        }

        PracticeProgress entity = (existing != null) ? existing : new PracticeProgress();

        entity.setUser(user);
        entity.setPracticeQuestion(question);
        entity.setPracticeAnswer(answer);

        entity.setAnsweredAt(LocalDateTime.now());
        entity.setIsCorrect(isCorrect);

        // 🔥 CHỈ set XP khi được cộng
        if (earnedXp > 0) {
            entity.setTotalXP(earnedXp);
        } else if (existing == null) {
            entity.setTotalXP(0);
        }
        // ❗ KHÔNG reset về 0 nếu đã từng đúng

        return practiceProgressRepository.save(entity);
    }

    private UserXPHistory addXpHistory(User user, int xp, int questionId) {
        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.PRACTICE);
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

    @Transactional
    public void updateUserPractice(Integer practiceId, Integer userId) {

        Integer totalXp = practiceProgressRepository.calculateTotalXp(practiceId, userId);

        int totalQuestions = practiceQuestionRepository.countByPracticeId(practiceId);

        int answered = practiceProgressRepository.countAnswered(practiceId, userId);

        int correct = practiceProgressRepository.countCorrect(practiceId, userId);

        double percent = answered == 0 ? 0 : (correct * 100.0 / answered);

        boolean isCompleted = answered >= totalQuestions && percent >= 70;

        UserPractice up = userPracticeRepository
                .findByUserIdAndPracticeId(userId, practiceId)
                .orElseGet(UserPractice::new); // 👉 đẹp hơn

        up.setUserId(userId);

        Practice practiceRef = practiceRepository.getReferenceById(practiceId);
        up.setPractice(practiceRef);

        up.setTotalXp(totalXp);
        up.setIsCompleted(isCompleted);

        userPracticeRepository.save(up);
    }


    private UserXPHistoryResponse buildResponse(UserXPHistory history,
                                                int earnedXp,
                                                Integer userId,
                                                Integer questionId) {

        UserXPHistoryResponse res = new UserXPHistoryResponse();

        res.setUserId(userId);
        res.setXp(earnedXp);
        res.setSource("PRACTICE");
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
        res.setSource("PRACTICE");
        res.setSourceId(questionId);
        res.setEarnedAt(LocalDateTime.now());

        return res;
    }

}

