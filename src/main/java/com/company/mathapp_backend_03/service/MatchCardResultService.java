package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.*;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchCardResultService {
    private final MatchCardResultRepository matchCardResultRepository;
    private final MatchCardRepository matchCardRepository;
    private final UserRepository userRepository;
    private final UserXPHistoryRepository historyRepository;
    private final UserStatRepository userStatRepository;

    public MatchCardResultResponse getMatchCardResult(Integer matchCardId, Integer userId) {
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

    @Transactional
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
    }

    @Transactional
    public UserXPHistoryResponse processMatchCardResult(MatchCardResultRequest request) {

        if (request.getCorrectPairs() > request.getTotalPairs()) {
            throw new IllegalArgumentException("Số cặp đúng không thể lớn hơn tổng số cặp.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy User"));

        MatchCard matchCard = matchCardRepository.findById(request.getMatchCardId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy MatchCard"));

        // 1. Tìm lịch sử chơi cũ để lấy kỷ lục cũ (Chống gian lận và ghi đè)
        MatchCardResult existingResult = matchCardResultRepository
                .findByMatchCardAndUser(matchCard, user)
                .orElse(null);

        int previousBestPairs = (existingResult != null && existingResult.getCorrectPairs() != null)
                ? existingResult.getCorrectPairs() : 0;

        int newCorrectPairs = request.getCorrectPairs();

        // 2. Chỉ tính XP cho những cặp MỚI ghép đúng (Vá lỗi Infinite XP)
        int earnedXp = 0;
        if (newCorrectPairs > previousBestPairs) {
            int newlySolvedPairs = newCorrectPairs - previousBestPairs;
            earnedXp = newlySolvedPairs * matchCard.getXpReward();
        }

        try {
            // 3. Truyền existingResult xuống để xử lý cập nhật
            updateMatchCardResultRecord(user, matchCard, request, existingResult, earnedXp);

            UserXPHistory history = null;
            if (earnedXp > 0) {
                history = addXpHistory(user, earnedXp, matchCard.getId());
                updateUserStats(user, earnedXp);
            }

            return history != null ? mapToResponse(history) : null;

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Thao tác quá nhanh, dữ liệu đang được xử lý.");
        }
    }

    private void updateMatchCardResultRecord(User user, MatchCard matchCard, MatchCardResultRequest request, MatchCardResult existingResult, int earnedXp) {
        MatchCardResult result = existingResult != null ? existingResult : new MatchCardResult();

        if (existingResult == null) {
            result.setMatchCard(matchCard);
            result.setUser(user);
            result.setTotalXP(0);
            result.setTotalPairs(request.getTotalPairs());
            result.setCorrectPairs(request.getCorrectPairs());
            result.setTimeTaken(request.getTimeTaken());
        } else {
            if (request.getCorrectPairs() > result.getCorrectPairs()) {
                result.setCorrectPairs(request.getCorrectPairs());
                result.setTimeTaken(request.getTimeTaken());
            }
            else if (request.getCorrectPairs().equals(result.getCorrectPairs()) && request.getTimeTaken() < result.getTimeTaken()) {
                result.setTimeTaken(request.getTimeTaken());
            }
        }

        int currentXP = result.getTotalXP() != null ? result.getTotalXP() : 0;
        result.setTotalXP(currentXP + earnedXp);

        matchCardResultRepository.save(result);
    }

    private UserXPHistory addXpHistory(User user, int xp, int matchCardId) {
        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(xp);
        history.setSource(Source.MATCH_CARD_GAME); // Phân biệt với FLASHCARD
        history.setSourcedId(matchCardId);
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
