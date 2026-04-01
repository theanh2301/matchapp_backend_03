package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.UserPractice;
import com.company.mathapp_backend_03.model.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPracticeRepository extends JpaRepository<UserPractice, Integer> {
    @Query("SELECT COUNT(up) FROM UserPractice up WHERE up.practice.practiceType = :practiceType AND up.isCompleted = true AND up.userId = :userId")
    Integer countCompletedByPracticeTypeAndUserId(@Param("practiceType") PracticeType practiceType, @Param("userId") Integer userId);

    Optional<UserPractice> findByUserIdAndPracticeId(Integer userId, Integer practiceId);
}