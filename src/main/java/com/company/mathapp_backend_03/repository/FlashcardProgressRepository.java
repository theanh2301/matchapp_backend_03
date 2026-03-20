package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Flashcard;
import com.company.mathapp_backend_03.entity.FlashcardProgress;
import com.company.mathapp_backend_03.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Integer> {

    List<FlashcardProgress> findByFlashcardIdAndUserId(Integer flashcardId, Integer userId);

    Optional<FlashcardProgress> findByFlashcardAndUser(Flashcard flashcard, User user);

    Optional<FlashcardProgress> findByUserIdAndFlashcardId(@NotNull(message = "userId cannot be null") Integer userId, @NotNull(message = "flashcardId cannot be null") Integer flashcardId);
}
