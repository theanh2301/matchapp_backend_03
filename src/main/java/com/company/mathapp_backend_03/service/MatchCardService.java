package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.MatchCard;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.ConflictException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.MatchCardRequest;
import com.company.mathapp_backend_03.model.response.MatchCardResponse;
import com.company.mathapp_backend_03.repository.LessonRepository;
import com.company.mathapp_backend_03.repository.MatchCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchCardService {
    private final MatchCardRepository matchCardRepository;
    private final LessonRepository lessonRepository;

    public List<MatchCardResponse> getMatchCard(Integer id) {
        List<MatchCard> getMatchCard = matchCardRepository.findByLessonId(id);

        return getMatchCard.stream().map(matchCard -> new MatchCardResponse(
                matchCard.getId(),
                matchCard.getPairId(),
                matchCard.getContent(),
                matchCard.getXpReward()
        )).toList();
    }

    public void addMatchCard(MatchCardRequest matchCardRequest) {

        Lesson lesson = lessonRepository.findById(matchCardRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        Optional<MatchCard> existingMatchGame = matchCardRepository.findByPairIdAndContentAndLesson(
                matchCardRequest.getPairId(),
                matchCardRequest.getContent(),
                lesson
            );

        if (existingMatchGame.isPresent()) {
            throw new BadRequestException("MatchCard already exists in this lesson");
        }

        MatchCard matchCard = MatchCard.builder()
                .pairId(matchCardRequest.getPairId())
                .content(matchCardRequest.getContent())
                .xpReward(matchCardRequest.getXpReward())
                .lesson(lesson)
                .build();

        matchCardRepository.save(matchCard);
    }

    public void updateMatchCard(Integer id, MatchCardRequest request) {

        MatchCard matchCard = matchCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MatchCard not found"));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        boolean isDuplicate = matchCardRepository
                .existsByPairIdAndContentAndLessonAndIdNot(
                        request.getPairId(),
                        request.getContent().trim(),
                        lesson,
                        id
                );

        if (isDuplicate) {
            throw new ConflictException("MatchCard already exists in this lesson");
        }

        boolean isSame = Objects.equals(matchCard.getPairId(), request.getPairId()) &&
                         Objects.equals(matchCard.getContent(), request.getContent()) &&
                         Objects.equals(matchCard.getLesson().getId(), lesson.getId());

        if (isSame) {
            throw new BadRequestException("No changes detected");
        }

        matchCard.setPairId(request.getPairId());
        matchCard.setContent(request.getContent().trim());
        matchCard.setLesson(lesson);
        matchCardRepository.save(matchCard);
    }

    public void deleteMatchCard(Integer id) {

        MatchCard matchCard = matchCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MatchCard not found"));

        matchCardRepository.delete(matchCard);
    }
}
