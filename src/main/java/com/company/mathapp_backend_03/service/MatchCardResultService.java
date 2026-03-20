package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.MatchCard;
import com.company.mathapp_backend_03.entity.MatchCardResult;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.model.request.MatchCardResultRequest;
import com.company.mathapp_backend_03.model.response.FlashcardProgressResponse;
import com.company.mathapp_backend_03.model.response.MatchCardResultResponse;
import com.company.mathapp_backend_03.repository.MatchCardRepository;
import com.company.mathapp_backend_03.repository.MatchCardResultRepository;
import com.company.mathapp_backend_03.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchCardResultService {
    private final MatchCardResultRepository matchCardResultRepository;
    private final MatchCardRepository matchCardRepository;
    private final UserRepository userRepository;


    public List<MatchCardResultResponse> getMatchCardResult(Integer matchCardId, Integer userId) {
        List<MatchCardResult> matchCardResults = matchCardResultRepository
                .findByMatchCardIdAndUserId(matchCardId, userId);

        return matchCardResults.stream().map(matchCardResult ->
                new MatchCardResultResponse(
                        matchCardResult.getId(),
                        matchCardResult.getTotalPairs(),
                        matchCardResult.getCorrectPairs(),
                        matchCardResult.getTimeTaken(),
                        matchCardResult.getTotalXP()
                )).toList();
    }

    @Transactional
    public void addOrUpdateMatchCardResult(MatchCardResultRequest matchCardResultRequest) {

        MatchCard matchCard = matchCardRepository.findById(matchCardResultRequest.getMatchCardId())
                .orElseThrow(() -> new EntityNotFoundException("MatchCard not found"));

        User user = userRepository.findById(matchCardResultRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (matchCardResultRequest.getCorrectPairs() > matchCardResultRequest.getTotalPairs()) {
            throw new IllegalArgumentException("Số cặp đúng không thể lớn hơn tổng số cặp.");
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
            throw new IllegalStateException("Thao tác quá nhanh, tiến độ đang được xử lý.");
        }
    }
}
