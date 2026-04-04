package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.UserStat;
import com.company.mathapp_backend_03.model.dto.XpByDateProjection;
import com.company.mathapp_backend_03.model.request.UserStatRequest;
import com.company.mathapp_backend_03.model.response.DashboardResponse;
import com.company.mathapp_backend_03.model.response.UserStatResponse;
import com.company.mathapp_backend_03.model.response.XpChartResponse;
import com.company.mathapp_backend_03.repository.UserStatRepository;
import com.company.mathapp_backend_03.repository.UserXPHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatService {

    private final UserStatRepository repository;
    private final UserXPHistoryRepository userXPHistoryRepository;

    public UserStatResponse getUserStats(Integer userId) {
        UserStat stats = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return mapToResponse(stats);
    }

    public List<XpChartResponse> getWeeklyXp(Integer userId, LocalDate date) {

        // 1. Tính đầu tuần (Monday)
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);

        // 2. Cuối tuần (Sunday)
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 3. Query DB
        List<XpByDateProjection> data = userXPHistoryRepository.getXpByDateRange(
                userId,
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23, 59, 59)
        );

        // 4. Convert sang Map
        Map<LocalDate, Integer> map = data.stream()
                .collect(Collectors.toMap(
                        XpByDateProjection::getDate,
                        XpByDateProjection::getTotalXp
                ));

        // 5. Fill đủ 7 ngày
        List<XpChartResponse> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate current = startOfWeek.plusDays(i);

            result.add(new XpChartResponse(
                    current,
                    map.getOrDefault(current, 0)
            ));
        }

        return result;
    }

    public DashboardResponse getDashboard(Integer userId, LocalDate date) {

        UserStatResponse stats = getUserStats(userId);

        List<XpChartResponse> weeklyXp = getWeeklyXp(
                userId,
                date != null ? date : LocalDate.now()
        );

        return new DashboardResponse(stats, weeklyXp);
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