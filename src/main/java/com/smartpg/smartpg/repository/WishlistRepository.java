package com.smartpg.smartpg.repository;

import com.smartpg.smartpg.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByUserIdAndPgId(Long userId, Long pgId);

    void deleteByUserIdAndPgId(Long userId, Long pgId);

    List<Wishlist> findByUserId(Long userId);

    int countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.pgId = :pgId")
    void deleteByPgId(@Param("pgId") Long pgId);
}
