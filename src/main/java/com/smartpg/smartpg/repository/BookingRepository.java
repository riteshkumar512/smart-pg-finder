package com.smartpg.smartpg.repository;

import com.smartpg.smartpg.model.Booking;
import com.smartpg.smartpg.model.User;
import com.smartpg.smartpg.model.PG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.pg.id = :pgId")
    void deleteByPgId(@Param("pgId") int pgId);
    boolean existsByUserAndPg(User user, PG pg);
    Booking findByUserAndPgAndStatus(User user, PG pg, String status);
    List<Booking> findByUser(User user);
    List<Booking> findByPg_Owner(User owner);

}
