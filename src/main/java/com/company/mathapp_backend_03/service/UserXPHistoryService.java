package com.company.mathapp_backend_03.service;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.entity.UserXPHistory;
import com.company.mathapp_backend_03.model.enums.Source;
import com.company.mathapp_backend_03.model.request.UserXPHistoryRequest;
import com.company.mathapp_backend_03.model.response.UserXPHistoryResponse;
import com.company.mathapp_backend_03.repository.UserRepository;
import com.company.mathapp_backend_03.repository.UserXPHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserXPHistoryService {

    private final UserXPHistoryRepository historyRepository;

    private final UserRepository userRepository;
    @Transactional
    public UserXPHistoryResponse addXpRecord(UserXPHistoryRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + request.getUserId()));

        UserXPHistory history = new UserXPHistory();
        history.setUser(user);
        history.setXp(request.getXp());

        history.setSource(Source.valueOf(request.getSource().toUpperCase()));

        history.setSourcedId(request.getSourceId());
        history.setEarnedAt(LocalDateTime.now());

        UserXPHistory savedRecord = historyRepository.save(history);

        return mapToResponse(savedRecord);
    }

    public List<UserXPHistoryResponse> getHistoryByUserId(Integer userId) {
        List<UserXPHistory> histories = historyRepository.findByUserId(userId);

        return histories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserXPHistoryResponse mapToResponse(UserXPHistory entity) {
        UserXPHistoryResponse response = new UserXPHistoryResponse();
        response.setId(entity.getId());

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
        }

        response.setXp(entity.getXp());

        if (entity.getSource() != null) {
            response.setSource(entity.getSource().name());
        }

        response.setSourceId(entity.getSourcedId());
        response.setEarnedAt(entity.getEarnedAt());
        return response;
    }
}