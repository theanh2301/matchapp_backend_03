package com.company.mathapp_backend_03.model.dto;

import java.time.LocalDate;

public interface XpByDateProjection {
    LocalDate getDate();
    Integer getTotalXp();
}