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
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardProgressService {
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    public List<FlashcardProgressResponse> getFlashcardProgressByFlashcardIdAndUserId(Integer flashcardId, Integer userId) {
        List<FlashcardProgress> flashcardProgresses = flashcardProgressRepository
                .findByFlashcardIdAndUserId(flashcardId, userId);

        return flashcardProgresses.stream().map(flashcardProgress ->
                new FlashcardProgressResponse(
                flashcardProgress.getId(),
                flashcardProgress.getIsKnown(),
                flashcardProgress.getLastReviewed(),
                flashcardProgress.getTotalXP()
        )).toList();
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
        progress.setLastReviewed(flashcardProgressRequest.getLastReviewed() != null ? flashcardProgressRequest.getLastReviewed() : LocalDate.now());

        int currentXP = (progress.getTotalXP() != null) ? progress.getTotalXP() : 0;
        progress.setTotalXP(currentXP + flashcardProgressRequest.getTotalXP());

        try {
            flashcardProgressRepository.save(progress);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Thao tác quá nhanh, tiến độ đang được xử lý.");
        }
    }

}
