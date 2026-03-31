package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardProgressService {
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository userXPHistoryRepository;
    private final UserStatRepository userStatRepository;

    public FlashcardProgress progress;

    public FlashcardProgressResponse getFlashcardProgress(Integer flashcardId, Integer userId) {
        Optional<FlashcardProgress> flashcardProgresses = flashcardProgressRepository.findByFlashcardIdAndUserId(flashcardId, userId);

        if (flashcardProgresses.isEmpty()) {
            return null;
        }

        progress = new FlashcardProgress();

        return new FlashcardProgressResponse(
                    progress.getId(),
                    progress.getIsKnown(),
                    progress.getLastReviewed(),
                    progress.getTotalXP()
                );
    }

    @Transactional
    public void addOrUpdateFlashcardProgress(FlashcardProgressRequest flashcardProgressRequest) {

        Flashcard flashcard = flashcardRepository.findById(flashcardProgressRequest.getFlashcardId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));


        User user = userRepository.findById(flashcardProgressRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        progress = flashcardProgressRepository
                .findByFlashcardAndUser(flashcard, user)
                .orElseGet(() -> {
                    FlashcardProgress newProgress = new FlashcardProgress();
                    newProgress.setFlashcard(flashcard);
                    newProgress.setUser(user);
                    newProgress.setTotalXP(0);
                    return newProgress;
                });

        progress.setIsKnown(flashcardProgressRequest.getIsKnown());
        progress.setLastReviewed(flashcardProgressRequest.getLastReviewed() != null ? flashcardProgressRequest.getLastReviewed() : LocalDateTime.now());

        int currentXP = (progress.getTotalXP() != null) ? progress.getTotalXP() : 0;
        progress.setTotalXP(currentXP + flashcardProgressRequest.getTotalXP());

        try {
            flashcardProgressRepository.save(progress);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Thao tác quá nhanh, tiến độ đang được xử lý.");
        }
    }

    @Transactional
    public UserXPHistoryResponse processFlashcardStudy(FlashcardProgressRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        Flashcard flashcard = flashcardRepository.findById(request.getFlashcardId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard không tồn tại"));

        FlashcardProgress progress = flashcardProgressRepository
                .findByUserIdAndFlashcardId(user.getId(), flashcard.getId())
                .orElse(null);

        boolean previouslyKnown = progress != null && Boolean.TRUE.equals(progress.getIsKnown());
        boolean newlyKnown = Boolean.TRUE.equals(request.getIsKnown());

        int earnedXp = (newlyKnown && !previouslyKnown) ? flashcard.getXpReward() : 0;

        // 1. Update progress
        updateFlashcardProgress(user, flashcard, request, progress, earnedXp);

        UserXPHistory history = null;

        if (earnedXp > 0) {

            boolean exists = existsXpHistory(user.getId(), flashcard.getId());

            if (!exists) {
                history = addXpHistory(user, earnedXp, flashcard.getId());
                updateUserStats(user, earnedXp);
            } else {
                history = userXPHistoryRepository
                        .findByUserIdAndSourcedIdAndSource(
                                user.getId(),
                                flashcard.getId(),
                                Source.FLASHCARD_GAME
                        )
                        .orElse(null);
            }
        }

        return buildResponse(user, flashcard, history);
    }

    private FlashcardProgress updateFlashcardProgress(
            User user,
            Flashcard flashcard,
            FlashcardProgressRequest request,
            FlashcardProgress progress,
            int earnedXp
    ) {

        if (progress == null) {
            progress = new FlashcardProgress();
            progress.setUser(user);
            progress.setFlashcard(flashcard);
            progress.setTotalXP(0);
        }

        progress.setIsKnown(request.getIsKnown());
        progress.setLastReviewed(LocalDateTime.now());

        if (earnedXp > 0) {
            progress.setTotalXP(
                    (progress.getTotalXP() == null ? 0 : progress.getTotalXP()) + earnedXp
            );
        }

        return flashcardProgressRepository.save(progress); // ❌ bỏ flush
    }

    @Transactional
    public UserXPHistory addXpHistory(User user, int earnedXp, Integer flashcardId) {

        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(earnedXp);
        history.setSourcedId(flashcardId);
        history.setSource(Source.FLASHCARD_GAME);
        history.setEarnedAt(LocalDateTime.now());

        return userXPHistoryRepository.save(history); // ❌ bỏ flush
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

        userStatRepository.save(stats); // ❌ bỏ flush
    }

    private boolean existsXpHistory(Integer userId, Integer flashcardId) {
        return userXPHistoryRepository
                .existsByUserIdAndSourcedIdAndSource(
                        userId,
                        flashcardId,
                        Source.FLASHCARD_GAME
                );
    }

    private UserXPHistoryResponse buildResponse(User user, Flashcard flashcard, UserXPHistory history) {

        if (history != null) {
            return UserXPHistoryResponse.builder()
                    .id(history.getId())
                    .xp(history.getXp())
                    .sourceId(history.getSourcedId())
                    .source(history.getSource().name())
                    .earnedAt(history.getEarnedAt())
                    .userId(history.getUser().getId())
                    .build();
        }

        return UserXPHistoryResponse.builder()
                .id(flashcard.getId())
                .xp(0)
                .source(Source.FLASHCARD_GAME.name())
                .sourceId(flashcard.getId())
                .userId(user.getId())
                .build();
    }
}
