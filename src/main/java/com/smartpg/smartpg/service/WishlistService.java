package com.smartpg.smartpg.service;

import com.smartpg.smartpg.model.Wishlist;
import com.smartpg.smartpg.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    // 🔁 Toggle Wishlist (ADD / REMOVE)
    public String toggleWishlist(Long userId, Long pgId) {

        boolean exists = wishlistRepository.existsByUserIdAndPgId(userId, pgId);

        System.out.println("EXISTS: " + exists);

        if (exists) {
            wishlistRepository.deleteByUserIdAndPgId(userId, pgId);
            wishlistRepository.flush();
            return "removed";
        } else {
            wishlistRepository.save(new Wishlist(userId, pgId));
            wishlistRepository.flush();
            return "added";
        }
    }

    // 📋 Get Wishlist
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // ❤️ Count Wishlist
    public int getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }

    // ❌ Remove manually
    public void remove(Long userId, Long pgId) {

        if(wishlistRepository.existsByUserIdAndPgId(userId, pgId)){
            wishlistRepository.deleteByUserIdAndPgId(userId, pgId);
        }
    }
    public void deleteByPgId(Long pgId) {
        wishlistRepository.deleteByPgId(pgId);
    }

}