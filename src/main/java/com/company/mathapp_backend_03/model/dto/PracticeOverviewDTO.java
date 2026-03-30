package com.company.mathapp_backend_03.model.dto;

import com.company.mathapp_backend_03.model.enums.Difficulty;

public interface PracticeOverviewDTO {
    Integer getId();
    String getTitle();
    String getDescription();
    Integer getTimeLimit();
    Difficulty getDifficulty();
    Integer getTotalQuestions();
    Integer getTotalXp();
}