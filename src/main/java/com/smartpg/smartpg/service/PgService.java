package com.smartpg.smartpg.service;

import com.smartpg.smartpg.model.PG;
import com.smartpg.smartpg.repository.PGRepository;
import com.smartpg.smartpg.repository.BookingRepository;
import com.smartpg.smartpg.repository.RatingRepository;
import com.smartpg.smartpg.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;

@Service
@Transactional
public class PgService {

    private final PGRepository pgRepository;
    private final BookingRepository bookingRepository;
    private final WishlistRepository wishlistRepository;
    private final RatingRepository ratingRepository;

    // Constructor (auto injection)
    public PgService(PGRepository pgRepository,
                     BookingRepository bookingRepository,
                     WishlistRepository wishlistRepository,
                     RatingRepository ratingRepository) {

        this.pgRepository = pgRepository;
        this.bookingRepository = bookingRepository;
        this.wishlistRepository = wishlistRepository;
        this.ratingRepository = ratingRepository;
    }

    public void deletePg(int id) {

        PG pg = pgRepository.findById(id).orElse(null);


        if (pg != null && pg.getImage() != null) {

            File file = new File("uploads/" + pg.getImage());

            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }

        bookingRepository.deleteByPgId(id);
        wishlistRepository.deleteByPgId((long) id);
        ratingRepository.deleteByPgId(id);

        pgRepository.deleteById(id);
    }
}