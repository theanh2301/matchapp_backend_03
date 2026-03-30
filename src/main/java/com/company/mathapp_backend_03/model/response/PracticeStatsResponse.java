package com.company.mathapp_backend_03.model.response;

import com.company.mathapp_backend_03.model.enums.PracticeType;

public record PracticeStatsResponse(
        PracticeType practiceType,
        Integer totalPractice,
        Integer completedPractice
) {
}
