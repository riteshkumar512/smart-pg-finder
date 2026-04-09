package com.smartpg.smartpg.repository;

import com.smartpg.smartpg.model.Rating;
import com.smartpg.smartpg.model.User;
import com.smartpg.smartpg.model.PG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.pg.id = :pgId")
    void deleteByPgId(@Param("pgId") int pgId);

    List<Rating> findByPg(PG pg);
    boolean existsByUserAndPg(User user, PG pg);
}