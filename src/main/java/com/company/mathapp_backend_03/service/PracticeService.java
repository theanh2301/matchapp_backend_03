package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import com.company.mathapp_backend_03.model.response.PracticeStatsGroupResponse;
import com.company.mathapp_backend_03.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_03.repository.PracticeRepository;
import com.company.mathapp_backend_03.repository.UserPracticeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final UserPracticeRepository userPracticeRepository;

    public PracticeStatsResponse getPracticeStats(PracticeType practiceType, Integer userId) {
        Integer total = practiceRepository.countByPracticeType(practiceType);
        Integer completed = userPracticeRepository.countCompletedByPracticeTypeAndUserId(practiceType, userId);

        return new PracticeStatsResponse(practiceType, total, completed);
    }

    public List<PracticeOverviewDTO> getPracticeOverview(PracticeType practiceType, Integer userId) {
        return practiceRepository.getPracticeOverviewWithProgress(
                practiceType.name(), userId
        );
    }

    public List<PracticeOverviewDTO> getPracticeOverviewWeak(Integer userId) {
        return practiceRepository.getPracticeOverviewWeak(userId);
    }

    public PracticeStatsGroupResponse getAllPracticeStats(Integer userId) {

        Map<PracticeType, Integer> totalMap = new HashMap<>();
        Map<PracticeType, Integer> completedMap = new HashMap<>();

        for (Object[] row : practiceRepository.countAllByPracticeType()) {
            totalMap.put((PracticeType) row[0], ((Long) row[1]).intValue());
        }

        for (Object[] row : practiceRepository.countCompletedGroupByType(userId)) {
            completedMap.put((PracticeType) row[0], ((Long) row[1]).intValue());
        }

        List<PracticeStatsResponse> result = new ArrayList<>();

        for (PracticeType type : PracticeType.values()) {
            result.add(new PracticeStatsResponse(
                    type,
                    totalMap.getOrDefault(type, 0),
                    completedMap.getOrDefault(type, 0)
            ));
        }

        return new PracticeStatsGroupResponse(result);
    }
}
