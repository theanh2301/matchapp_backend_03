package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Flashcard;
import com.company.mathapp_backend_03.entity.FlashcardProgress;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.model.request.FlashcardProgressRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.repository.FlashcardProgressRepository;
import com.company.mathapp_backend_03.repository.FlashcardRepository;
import com.company.mathapp_backend_03.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardProgressService {
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    public FlashcardProgressResponse getFlashcardProgress(Integer flashcardId, Integer userId) {
        Optional<FlashcardProgress> flashcardProgresses = flashcardProgressRepository.findByFlashcardIdAndUserId(flashcardId, userId);

        if (flashcardProgresses.isEmpty()) {
            return null;
        }

        FlashcardProgress flashcardProgress = new FlashcardProgress();

        return new FlashcardProgressResponse(
                    flashcardProgress.getId(),
                    flashcardProgress.getIsKnown(),
                    flashcardProgress.getLastReviewed(),
                    flashcardProgress.getTotalXP()
                );
    }

    @Transactional
    public void addOrUpdateFlashcardProgress(FlashcardProgressRequest flashcardProgressRequest) {

        Flashcard flashcard = flashcardRepository.findById(flashcardProgressRequest.getFlashcardId())
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));


        User user = userRepository.findById(flashcardProgressRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        FlashcardProgress progress = flashcardProgressRepository
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

}
