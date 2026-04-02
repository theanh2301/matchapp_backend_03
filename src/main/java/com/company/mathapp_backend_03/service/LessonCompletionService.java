package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.LessonCompletion;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.repository.LessonCompletionRepository;
import com.company.mathapp_backend_03.repository.LessonRepository;
import com.company.mathapp_backend_03.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LessonCompletionService {

    private final LessonCompletionRepository lessonCompletionRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void updateLessonProgress(
            Integer userId,
            Integer lessonId,
            Integer xpGained,
            Source source
    ) {

        LessonCompletion lc = lessonCompletionRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    // ✅ TẠO MỚI nếu chưa có
                    LessonCompletion newLc = new LessonCompletion();

                    newLc.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found")));

                    newLc.setLesson(lessonRepository.findById(lessonId)
                            .orElseThrow(() -> new RuntimeException("Lesson not found")));

                    newLc.setTotalXp(0);

                    newLc.setIsFlashcardCompleted(false);
                    newLc.setIsMatchCardCompleted(false);
                    newLc.setIsQuizCompleted(false);
                    newLc.setIsCompleted(false);

                    newLc.setUpdatedAt(LocalDateTime.now());

                    return newLc;
                });

        // ✅ UPDATE theo từng game + tránh cộng XP trùng
        switch (source) {

            case FLASHCARD_GAME:
                if (!Boolean.TRUE.equals(lc.getIsFlashcardCompleted())) {
                    if (xpGained > 0) {
                        lc.setTotalXp(lc.getTotalXp() + xpGained);
                    }
                    lc.setIsFlashcardCompleted(true);
                }
                break;

            case MATCH_CARD_GAME:
                if (!Boolean.TRUE.equals(lc.getIsMatchCardCompleted())) {
                    if (xpGained > 0) {
                        lc.setTotalXp(lc.getTotalXp() + xpGained);
                    }
                    lc.setIsMatchCardCompleted(true);
                }
                break;

            case QUIZ_GAME:
                if (!Boolean.TRUE.equals(lc.getIsQuizCompleted())) {
                    if (xpGained > 0) {
                        lc.setTotalXp(lc.getTotalXp() + xpGained);
                    }
                    lc.setIsQuizCompleted(true);
                }
                break;
        }

        // ✅ CHECK hoàn thành lesson
        if (Boolean.TRUE.equals(lc.getIsFlashcardCompleted())
                && Boolean.TRUE.equals(lc.getIsMatchCardCompleted())
                && Boolean.TRUE.equals(lc.getIsQuizCompleted())) {

            lc.setIsCompleted(true);

            if (lc.getCompletedAt() == null) {
                lc.setCompletedAt(LocalDateTime.now());
            }
        }

        lc.setUpdatedAt(LocalDateTime.now());

        // ✅ SAVE (insert hoặc update đều dùng save)
        lessonCompletionRepository.save(lc);
    }

}
