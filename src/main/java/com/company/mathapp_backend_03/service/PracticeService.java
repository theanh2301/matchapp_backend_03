package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.model.dto.PracticeOverviewDTO;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import com.company.mathapp_backend_03.model.response.PracticeStatsResponse;
import com.company.mathapp_backend_03.repository.PracticeRepository;
import com.company.mathapp_backend_03.repository.UserPracticeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
