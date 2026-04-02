package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.MatchCardResultRequest;
import com.company.mathapp_backend_03.model.response.MatchCardResultResponse;
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
public class MatchCardResultService {
    private final MatchCardResultRepository matchCardResultRepository;
    private final MatchCardRepository matchCardRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository historyRepository;
    private final UserStatRepository userStatRepository;
    private final LessonRepository lessonRepository;
    private final LessonCompletionService lessonCompletionService;

   /* public MatchCardResultResponse getMatchCardResult(Integer matchCardId, Integer userId) {
        Optional<MatchCardResult> matchCardResults = matchCardResultRepository.findByMatchCardIdAndUserId(matchCardId, userId);

        if (matchCardResults.isEmpty()) {
            return null;
        }

        MatchCardResult matchCardResult = new MatchCardResult();

        return new MatchCardResultResponse(
                        matchCardResult.getId(),
                        matchCardResult.getTotalPairs(),
                        matchCardResult.getCorrectPairs(),
                        matchCardResult.getTimeTaken(),
                        matchCardResult.getTotalXP()
                );
    }
*/
   /* @Transactional
    public void addOrUpdateMatchCardResult(MatchCardResultRequest matchCardResultRequest) {

        MatchCard matchCard = matchCardRepository.findById(matchCardResultRequest.getMatchCardId())
                .orElseThrow(() -> new EntityNotFoundException("MatchCard not found"));

        User user = userRepository.findById(matchCardResultRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (matchCardResultRequest.getCorrectPairs() > matchCardResultRequest.getTotalPairs()) {
            throw new IllegalArgumentException("Correct pairs cannot exceed total pairs.");
        }

        MatchCardResult result = matchCardResultRepository
                .findByMatchCardAndUser(matchCard, user)
                .orElseGet(() -> {
                   MatchCardResult newResult = new MatchCardResult();
                   newResult.setMatchCard(matchCard);
                   newResult.setUser(user);
                   newResult.setTotalXP(0);
                   return newResult;
                });

        result.setTotalPairs(matchCardResultRequest.getTotalPairs());
        result.setCorrectPairs(matchCardResultRequest.getCorrectPairs());
        result.setTimeTaken(matchCardResultRequest.getTimeTaken());

        int currentXP = (result.getTotalXP() != null) ? result.getTotalXP() : 0;
        result.setTotalXP(currentXP + matchCardResultRequest.getTotalXP());

        try {
            matchCardResultRepository.save(result);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Operation too fast, progress is being processed.");
        }
    }*/

    @Transactional
    public void processMatchCardBatch(List<MatchCardResultRequest> requests) {

        if (requests == null || requests.isEmpty()) return;

        // ===== 1. USER =====
        Integer userId = requests.get(0).getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        // ===== 2. LOAD LESSON =====
        List<Integer> lessonIds = requests.stream()
                .map(MatchCardResultRequest::getLessonId)
                .distinct()
                .toList();

        Map<Integer, Lesson> lessonMap = lessonRepository.findAllById(lessonIds)
                .stream()
                .collect(Collectors.toMap(Lesson::getId, l -> l));

        // ===== 3. LOAD RESULT CŨ =====
        List<MatchCardResult> existingResults =
                matchCardResultRepository.findByUserIdAndLessonIdIn(userId, lessonIds);

        Map<Integer, MatchCardResult> resultMap = existingResults.stream()
                .collect(Collectors.toMap(r -> r.getLesson().getId(), r -> r));

        // ===== 4. LOAD HISTORY =====
        List<UserXPHistory> historyList =
                historyRepository.findByUserIdAndSourcedIdInAndSource(
                        userId,
                        lessonIds,
                        Source.MATCH_CARD_GAME
                );

        Set<Integer> existingHistoryIds = historyList.stream()
                .map(UserXPHistory::getSourcedId)
                .collect(Collectors.toSet());

        // ===== 5. PREPARE =====
        List<MatchCardResult> resultsToSave = new ArrayList<>();
        List<UserXPHistory> historyToSave = new ArrayList<>();

        int totalXpGained = 0;

        // 🔥 QUAN TRỌNG: XP theo lesson
        Map<Integer, Integer> xpByLesson = new HashMap<>();

        // ===== 6. LOOP =====
        for (MatchCardResultRequest request : requests) {

            Lesson lesson = lessonMap.get(request.getLessonId());
            if (lesson == null) continue;

            MatchCardResult result = resultMap.get(lesson.getId());

            // ❗ Backend nên tự tính XP (gợi ý)
            int earnedXp = calculateMatchCardXp(request);

            // ===== CREATE / UPDATE =====
            if (result == null) {
                result = new MatchCardResult();
                result.setUser(user);
                result.setLesson(lesson);
            }

            result.setTotalPairs(request.getTotalPairs());
            result.setCorrectPairs(request.getCorrectPairs());
            result.setTimeTaken(request.getTimeTaken());

            result.setTotalXP(
                    (result.getTotalXP() == null ? 0 : result.getTotalXP()) + earnedXp
            );

            resultsToSave.add(result);

            // ===== XP HISTORY =====
            if (earnedXp > 0 && !existingHistoryIds.contains(lesson.getId())) {

                UserXPHistory history = new UserXPHistory();
                history.setUser(user);
                history.setXp(earnedXp);
                history.setSource(Source.MATCH_CARD_GAME);
                history.setSourcedId(lesson.getId());

                historyToSave.add(history);

                totalXpGained += earnedXp;

                // 🔥 cộng theo lesson
                xpByLesson.merge(lesson.getId(), earnedXp, Integer::sum);
            }
        }

        // ===== 7. SAVE =====
        matchCardResultRepository.saveAll(resultsToSave);

        if (!historyToSave.isEmpty()) {
            historyRepository.saveAll(historyToSave);
        }

        // ===== 8. UPDATE USER =====
        if (totalXpGained > 0) {
            updateUserStats(user, totalXpGained);
        }

        // ===== 9. UPDATE LESSON COMPLETION =====
        for (Map.Entry<Integer, Integer> entry : xpByLesson.entrySet()) {
            lessonCompletionService.updateLessonProgress(
                    userId,
                    entry.getKey(),
                    entry.getValue(),
                    Source.MATCH_CARD_GAME
            );
        }
    }

    private int calculateMatchCardXp(MatchCardResultRequest request) {

        int correct = request.getCorrectPairs();
        int total = request.getTotalPairs();
        int time = request.getTimeTaken();

        if (total == 0) return 0;

        double accuracy = (double) correct / total;

        int baseXp = (int) (accuracy * 100);

        // thưởng nếu nhanh
        if (time < 60) {
            baseXp += 20;
        }

        return baseXp;
    }

    @Transactional
    public UserXPHistoryResponse processMatchCardResult(MatchCardResultRequest request) {

        if (request.getCorrectPairs() > request.getTotalPairs()) {
            throw new BadRequestException("Correct pairs cannot exceed total pairs");
        }

        if (request.getCorrectPairs() < 0) {
            throw new BadRequestException("Correct pairs invalid");
        }

        if (request.getTimeTaken() <= 0) {
            throw new BadRequestException("Time taken must be > 0");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        List<MatchCard> cards = matchCardRepository.findByLesson(lesson);

        if (cards.isEmpty()) {
            throw new BadRequestException("Lesson has no match cards");
        }

        int xpPerPair = cards.get(0).getXpReward();

        MatchCardResult existingResult = matchCardResultRepository
                .findByLessonAndUser(lesson, user)
                .orElse(null);

        int previousBestPairs = (existingResult != null && existingResult.getCorrectPairs() != null)
                ? existingResult.getCorrectPairs() : 0;

        int newCorrectPairs = request.getCorrectPairs();

        int earnedXp = 0;
        if (newCorrectPairs > previousBestPairs) {
            int newlySolvedPairs = newCorrectPairs - previousBestPairs;
            earnedXp = newlySolvedPairs * xpPerPair;
        }

        try {
            updateMatchCardResultRecord(user, lesson, request, existingResult, earnedXp);

            UserXPHistory history = null;
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, lesson.getId());
                updateUserStats(user, earnedXp);
            }

            return history != null ? mapToResponse(history) : null;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Request processed too quickly");
        }
    }

    private void updateMatchCardResultRecord(User user,
                                             Lesson lesson,
                                             MatchCardResultRequest request,
                                             MatchCardResult existingResult,
                                             int earnedXp) {

        MatchCardResult result = existingResult != null ? existingResult : new MatchCardResult();

        if (existingResult == null) {
            result.setLesson(lesson);
            result.setUser(user);
            result.setTotalXP(0);
            result.setTotalPairs(request.getTotalPairs());
            result.setCorrectPairs(request.getCorrectPairs());
            result.setTimeTaken(request.getTimeTaken());
        } else {
            // Update best score
            if (request.getCorrectPairs() > result.getCorrectPairs()) {
                result.setCorrectPairs(request.getCorrectPairs());
                result.setTimeTaken(request.getTimeTaken());
            }
            // Same score → lấy time tốt hơn
            else if (request.getCorrectPairs().equals(result.getCorrectPairs())
                    && request.getTimeTaken() < result.getTimeTaken()) {

                result.setTimeTaken(request.getTimeTaken());
            }
        }

        int currentXP = result.getTotalXP() != null ? result.getTotalXP() : 0;
        result.setTotalXP(currentXP + earnedXp);

        matchCardResultRepository.save(result);
    }

    private UserXPHistory addXpHistory(User user, int xp, int lessonId) {

        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.MATCH_CARD_GAME);
        history.setSourcedId(lessonId);
        history.setEarnedAt(LocalDateTime.now());

        return historyRepository.save(history);
    }

    private void updateUserStats(User user, int earnedXp) {

        UserStat stats = userStatRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserStat newStats = new UserStat();
                    newStats.setUserId(user.getId());
                    newStats.setTotalXP(0);
                    return newStats;
                });

        int currentXP = stats.getTotalXP() != null ? stats.getTotalXP() : 0;
        stats.setTotalXP(currentXP + earnedXp);

        userStatRepository.save(stats);
    }

    private UserXPHistoryResponse mapToResponse(UserXPHistory entity) {

        UserXPHistoryResponse response = new UserXPHistoryResponse();

        response.setId(entity.getId());
        response.setUserId(entity.getUser().getId());
        response.setXp(entity.getXp());
        response.setSource(entity.getSource().name());
        response.setSourceId(entity.getSourcedId());
        response.setEarnedAt(entity.getEarnedAt());

        return response;
    }
}
