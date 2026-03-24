package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Integer> {
}
