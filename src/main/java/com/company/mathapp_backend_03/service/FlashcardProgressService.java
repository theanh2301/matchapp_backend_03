package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
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
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User với ID: " + request.getUserId()));

        Flashcard flashcard = flashcardRepository.findById(request.getFlashcardId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

        FlashcardProgress existingProgress = flashcardProgressRepository
                .findByFlashcardAndUser(flashcard, user)
                .orElse(null);

        int earnedXp = 0;

        boolean previouslyKnown = existingProgress != null && Boolean.TRUE.equals(existingProgress.getIsKnown());
        boolean newlyKnown = Boolean.TRUE.equals(request.getIsKnown());

        if (newlyKnown && !previouslyKnown) {
            earnedXp = flashcard.getXpReward();
        }

        try {
            updateFlashcardProgress(user, flashcard, request, existingProgress, earnedXp);

            UserXPHistory history = null;
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, flashcard.getId());
                updateUserStats(user, earnedXp);
            }

            return history != null ? mapToResponse(history) : null;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Thao tác quá nhanh, tiến độ đang được xử lý.");
        }
    }

    private void updateFlashcardProgress(User user, Flashcard flashcard, FlashcardProgressRequest request, FlashcardProgress existingProgress, int earnedXp) {

        FlashcardProgress progress = existingProgress != null ? existingProgress : new FlashcardProgress();

        if (existingProgress == null) {
            progress.setFlashcard(flashcard);
            progress.setUser(user);
            progress.setTotalXP(0);
        }

        progress.setIsKnown(request.getIsKnown());
        progress.setLastReviewed(request.getLastReviewed() != null ? request.getLastReviewed() : LocalDateTime.now());

        int currentXP = progress.getTotalXP() != null ? progress.getTotalXP() : 0;
        progress.setTotalXP(currentXP + earnedXp);

        flashcardProgressRepository.save(progress);
    }

    private UserXPHistory addXpHistory(User user, int xp, int flashcardId) {
        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.FLASHCARD_GAME); // Hardcode Source hoặc dùng Enum Source.FLASHCARD
        history.setSourcedId(flashcardId);
        history.setEarnedAt(LocalDateTime.now());

        return userXPHistoryRepository.save(history);
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
