package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.UserStat;
import com.company.mathapp_backend_03.model.request.UserStatRequest;
import com.company.mathapp_backend_03.model.response.UserStatResponse;
import com.company.mathapp_backend_03.repository.UserStatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserStatService {

    private final UserStatRepository repository;

    public UserStatResponse getUserStats(Integer userId) {
        UserStat stats = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToResponse(stats);
    }

    @Transactional
    public UserStatResponse updateStats(Integer userId, UserStatRequest request) {
        UserStat stats = repository.findById(userId).orElse(new UserStat());

        stats.setUserId(userId);
        stats.setTotalXP(request.getTotalXP());
        stats.setTotalLesson(request.getTotalLesson());
        stats.setStreakDay(request.getStreakDay());
        stats.setLastStudyDate(LocalDateTime.now());

        UserStat updated = repository.save(stats);
        return mapToResponse(updated);
    }

    private UserStatResponse mapToResponse(UserStat entity) {
        // Có thể dùng ModelMapper hoặc MapStruct để tối ưu
        UserStatResponse res = new UserStatResponse();
        res.setUserId(entity.getUserId());
        res.setTotalXP(entity.getTotalXP());
        res.setTotalLesson(entity.getTotalLesson());
        res.setStreakDay(entity.getStreakDay());
        res.setLastDayStudy(entity.getLastStudyDate());
        return res;
    }
}