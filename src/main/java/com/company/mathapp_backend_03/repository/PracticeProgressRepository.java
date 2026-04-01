package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.PracticeProgress;
import com.company.mathapp_backend_03.entity.PracticeQuestion;
import com.company.mathapp_backend_03.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PracticeProgressRepository extends JpaRepository<PracticeProgress, Integer> {
    Optional<PracticeProgress> findByUserAndPracticeQuestion(User user, PracticeQuestion question);
}
