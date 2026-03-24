package com.company.mathapp_backend_03.model.dto;

public interface SubjectOverviewDTO {
    Integer getSubjectId();
    Integer subjectClass();
    String getSubjectName();
    String getIcon();

    Integer getTotalLessons();
    Integer getCompletedLessons();

    Integer getEarnedXp();
    Integer getTotalXp();
}