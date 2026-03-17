package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.MatchGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchGameRepository extends JpaRepository<MatchGame, Integer> {
    boolean existsByLessonId(Integer id);
}
