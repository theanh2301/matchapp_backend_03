package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.MatchCard;
import com.company.mathapp_backend_03.entity.MatchCardResult;
import com.company.mathapp_backend_03.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchCardResultRepository extends JpaRepository<MatchCardResult, Integer> {
    List<MatchCardResult> findByMatchCardIdAndUserId(Integer matchCardId, Integer userId);

    Optional<MatchCardResult> findByMatchCardAndUser(MatchCard matchCard, User user);
}
