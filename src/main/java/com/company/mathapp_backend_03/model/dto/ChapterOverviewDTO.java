package com.company.mathapp_backend_03.model.dto;

public interface ChapterOverviewDTO {
    Integer getChapterId();
    String getChapterName();
    String getDescription();
    
    Integer getTotalLessons();
    Integer getCompletedLessons();
    
    Integer getEarnedXp();
    Integer getTotalPossibleXp();
}