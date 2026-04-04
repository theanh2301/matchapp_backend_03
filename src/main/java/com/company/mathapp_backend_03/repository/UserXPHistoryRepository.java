package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.UserXPHistory;
import com.company.mathapp_backend_03.model.dto.XpByDateProjection;
import com.company.mathapp_backend_03.model.enums.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserXPHistoryRepository extends JpaRepository<UserXPHistory, Integer> {

    List<UserXPHistory> findByUserId(Integer userId);

    boolean existsByUserIdAndSourcedIdAndSource(Integer userId, Integer flashcardId, Source source);

    Optional<UserXPHistory> findByUserIdAndSourcedIdAndSource(Integer id, Integer id1, Source source);

    List<UserXPHistory> findByUserIdAndSourcedIdInAndSource(Integer userId, List<Integer> flashcardIds, Source source);

    @Query(value = """
    SELECT 
        DATE(earned_at) AS date,
        SUM(xp) AS totalXp
    FROM xp_history
    WHERE user_id = :userId
      AND earned_at BETWEEN :startDate AND :endDate
    GROUP BY DATE(earned_at)
    ORDER BY date
""", nativeQuery = true)
    List<XpByDateProjection> getXpByDateRange(
            Integer userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
