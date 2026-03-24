package com.company.mathapp_backend_03.model.dto;

public interface SubjectOverviewDTO {
    Integer getSubjectId();
    String getSubjectName();
    String getIcon();
    
    Integer getTotalChapters();
    Integer getTotalLessons();
    Integer getCompletedLessons();
    Integer getTotalXp();
}