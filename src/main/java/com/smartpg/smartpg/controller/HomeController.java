package com.smartpg.smartpg.controller;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartpg.smartpg.model.*;
import com.smartpg.smartpg.repository.PGRepository;
import com.smartpg.smartpg.service.PgService;
import org.springframework.beans.factory.annotation.Autowired;
import com.smartpg.smartpg.repository.UserRepository;
import com.smartpg.smartpg.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;
//image handle
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
//rating

//wishlist
import com.smartpg.smartpg.service.WishlistService;

import com.smartpg.smartpg.repository.RatingRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.ArrayList;

import java.util.*;

@Controller

public class HomeController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PGRepository pgRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private PgService pgService;
    @Autowired
    private Cloudinary cloudinary;

    private final WishlistService wishlistService;

    public HomeController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }
    @ModelAttribute
    public void addWishlistCount(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            int count = wishlistService.getWishlistCount(Long.valueOf(userId));
            model.addAttribute("wishlistCount", count);
        } else {
            model.addAttribute("wishlistCount", 0);
        }
    }


    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    //home page
    @GetMapping("/home")
    public String homePage(HttpSession session) {

        if(session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        return "home";
    }

    //Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            Model model,
                            HttpSession session) {

        User user = userRepository.findByUsernameAndPassword(username, password);

        if(user != null) {

            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // 🔥 ROLE BASED REDIRECT
            if(user.getRole().equals("owner")) {
                return "redirect:/owner/dashboard";
            } else {
                return "redirect:/home";
            }

        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }
    //Register page
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String phone) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);

        userRepository.save(user);

        return "login";
    }
    //book pg
    @GetMapping("/book/{id}")
    public String bookPG(@PathVariable int id, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if(userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);
        PG pg = pgRepository.findById(id).orElse(null);

        if(user == null || pg == null) {
            return "redirect:/pgs";
        }

        if(bookingRepository.existsByUserAndPg(user, pg)) {
            return "redirect:/pgs?error=already_booked";
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setPg(pg);

        booking.setStatus("PENDING");
        booking.setPaymentStatus("PENDING");
        bookingRepository.save(booking);
        wishlistService.remove(Long.valueOf(userId), Long.valueOf(id));

        return "redirect:/payment/" + booking.getId();
    }

    //booking data
    @GetMapping("/my-bookings")
    public String myBookings(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingRepository.findAll();

        List<Map<String, Object>> data = new ArrayList<>();

        for (Booking b : bookings) {

            if (b.getUser() != null && b.getUser().getId() == userId) {

                PG pg = b.getPg();

                if (pg != null) {

                    boolean alreadyRated = ratingRepository.existsByUserAndPg(user, pg);

                    Map<String, Object> map = new HashMap<>();
                    map.put("pg", pg);
                    map.put("bookingId", b.getId());
                    map.put("booking", b);
                    map.put("rated", alreadyRated); // ⭐ IMPORTANT

                    data.add(map);
                }
            }
        }

        model.addAttribute("data", data);

        return "mybookings";
    }
    //cancel button
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable int id) {

        bookingRepository.deleteById(id);

        return "redirect:/my-bookings";
    }
    //logout
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    //pgs
    @GetMapping("/pgs")
    public String showPGs(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer rent,
            Model model,
            HttpSession session) {

        List<PG> pgList = pgRepository.findAll();

        List<PG> filtered = new ArrayList<>();

        for (PG pg : pgList) {

            boolean cityMatch = true;
            boolean locationMatch = true;
            boolean rentMatch = true;

            // ✅ CITY FILTER
            if (city != null && !city.trim().isEmpty()) {
                cityMatch = pg.getCity() != null &&
                        pg.getCity().trim().equalsIgnoreCase(city.trim());
            }

            // ✅ LOCATION FILTER
            if (location != null && !location.trim().isEmpty()) {
                String searchLocation = location.trim().toLowerCase();

                locationMatch =
                        (pg.getLocation() != null &&
                                pg.getLocation().toLowerCase().contains(searchLocation))
                                ||
                                (pg.getCity() != null &&
                                        pg.getCity().toLowerCase().contains(searchLocation));
            }

            // ✅ RENT FILTER
            if (rent != null && rent > 1000) {
                rentMatch = pg.getPrice() <= rent;
            }

            // ✅ FINAL CHECK
            if (cityMatch && locationMatch && rentMatch) {
                filtered.add(pg);
            }
        }
        //Reviews
        List<Map<String, Object>> data = new ArrayList<>();

        for (PG pg : filtered) {

            Map<String, Object> map = new HashMap<>();

            List<Rating> reviews = ratingRepository.findByPg(pg);

            String shortReview = "";

            for (Rating r : reviews) {

                String full = r.getReview();

                if (full != null && !full.trim().isEmpty()) {

                    if (full.length() > 20) {
                        shortReview = full.substring(0, 20) + "...";
                    } else {
                        shortReview = full;
                    }

                    break;
                }
            }

            map.put("pg", pg);
            map.put("shortReview", shortReview);

            data.add(map);
        }

        model.addAttribute("data", data);
        //wishlist
        Integer userId = (Integer) session.getAttribute("userId");
        Set<Integer> wishlistIds = new HashSet<>();
        if(userId != null){

            List<Wishlist> wishlist = wishlistService.getWishlist(Long.valueOf(userId));

            for(Wishlist w : wishlist){
                wishlistIds.add(Math.toIntExact(w.getPgId())); // convert Long → Integer
            }

        }
        model.addAttribute("wishlistIds", wishlistIds);

        int count = wishlistService.getWishlistCount(Long.valueOf(userId));
        model.addAttribute("wishlistCount", count);

        return "pgs";
    }
    //search
    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

//    Add Pgs
@GetMapping("/add-pg")
public String addPgPage(HttpSession session) {

    String role = (String) session.getAttribute("role");

    if(role == null || !role.equals("owner")) {
        return "redirect:/home";
    }

    return "add-pg";
}
    @PostMapping("/add-pg")
    public String savePg(@RequestParam String name,
                         @RequestParam String city,
                         @RequestParam String location,
                         @RequestParam int price,
                         @RequestParam(value = "image", required = false) MultipartFile file,
                         HttpSession session) {

        try {

            Integer userId = (Integer) session.getAttribute("userId");
            User owner = userRepository.findById(userId).orElse(null);

            String fileName = null;

            // ✅ SAVE PG
            PG pg = new PG();
            pg.setName(name);
            pg.setCity(city);
            pg.setLocation(location);
            pg.setPrice(price);
            pg.setOwner(owner);

            if(file != null && !file.isEmpty()) {

                Map uploadResult = cloudinary.uploader()
                        .upload(file.getBytes(), ObjectUtils.emptyMap());

                String imageUrl = uploadResult.get("secure_url").toString();

                pg.setImage(imageUrl);
            }


            pgRepository.save(pg);

        } catch (Exception e) {
            e.printStackTrace(); // console for error
        }

        return "redirect:/owner/pgs";
    }
    //owner/pg
    @GetMapping("/owner/pgs")
    public String ownerPGs(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if(userId == null || role == null || !role.equals("owner")) {
            return "redirect:/login";
        }

        List<PG> allPGs = pgRepository.findAll();
        List<PG> myPGs = new ArrayList<>();

        for(PG pg : allPGs) {
            if(pg.getOwner() != null && pg.getOwner().getId() == userId) {
                myPGs.add(pg);
            }
        }

        model.addAttribute("pgList", myPGs);

        return "owner-pgs";
    }
    //owner /bookings
    @GetMapping("/owner/bookings")
    public String ownerBookings(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if(userId == null || role == null || !role.equals("owner")) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingRepository.findAll();
        List<Map<String, Object>> data = new ArrayList<>();

        for(Booking b : bookings) {

            PG pg = b.getPg();

            if(pg != null && pg.getOwner() != null &&
                    pg.getOwner().getId() == userId) {

                Map<String, Object> map = new HashMap<>();
                map.put("pg", pg);
                map.put("user", b.getUser()); // 👤 who booked
                map.put("booking", b);

                data.add(map);
            }
        }

        model.addAttribute("data", data);

        return "owner-bookings";
    }
    //edit by owner
    @GetMapping("/edit-pg/{id}")
    public String editPg(@PathVariable int id, Model model) {

        PG pg = pgRepository.findById(id).orElse(null);

        model.addAttribute("pg", pg);

        return "edit-pg";
    }
    @PostMapping("/edit-pg")
    public String updatePg(@RequestParam int id,
                           @RequestParam String name,
                           @RequestParam String city,
                           @RequestParam String location,
                           @RequestParam int price,
                           @RequestParam(value = "image", required = false) MultipartFile file) {

        try {

            PG pg = pgRepository.findById(id).orElse(null);

            if(pg != null) {

                pg.setName(name);
                pg.setCity(city);
                pg.setLocation(location);
                pg.setPrice(price);

                // ✅ IF NEW IMAGE UPLOADED
                if(file != null && !file.isEmpty()) {

                    Map uploadResult = cloudinary.uploader()
                            .upload(file.getBytes(), ObjectUtils.emptyMap());

                    String imageUrl = uploadResult.get("secure_url").toString();

                    pg.setImage(imageUrl);  // 🔥 IMPORTANT
                }

                pgRepository.save(pg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/owner/pgs";
    }
    //delete pg by owner
    @GetMapping("/delete-pg/{id}")
    public String deletePg(@PathVariable int id, HttpSession session) {

        String role = (String) session.getAttribute("role");

        if(role == null || !role.equals("owner")) {
            return "redirect:/home";
        }

        pgService.deletePg(id);   // ✅ CALL SERVICE

        return "redirect:/owner/pgs";
    }
    //Booking confirmation by owner
    @GetMapping("/booking/confirm/{id}")
    public String confirmBooking(@PathVariable int id,
                                 @RequestParam(required = false) String source,
                                 HttpSession session) {

        String role = (String) session.getAttribute("role");

        // 🔒 only owner allowed
        if(role == null || !role.equals("owner")) {
            return "redirect:/home";
        }

        Booking booking = bookingRepository.findById(id).orElse(null);

        if(booking != null){
            if (!"SUCCESS".equals(booking.getPaymentStatus())) {
                return "redirect:/owner/dashboard?error=Payment not done";
            }
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
        }
        if ("dashboard".equals(source)) {
            return "redirect:/owner/dashboard";
        }

        return "redirect:/owner/bookings";
    }
    //Rating functionility
    @PostMapping("/rate/{pgId}")
    public String ratePG(@PathVariable int pgId,
                         @RequestParam int stars,
                         @RequestParam(required = false) String review,
                         HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if(userId == null){
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);
        PG pg = pgRepository.findById(pgId).orElse(null);

        if(user == null || pg == null){
            return "redirect:/pgs";
        }

        // ✅ Check confirmed booking
        Booking booking = bookingRepository
                .findByUserAndPgAndStatus(user, pg, "CONFIRMED");

        if (booking == null) {
            return "redirect:/pgs?error=not_allowed";
        }

        // ✅ CHECK IF ALREADY RATED
        boolean alreadyRated = ratingRepository.existsByUserAndPg(user, pg);

        if(alreadyRated){
            return "redirect:/my-bookings"; // prevent duplicate
        }

        // ✅ Save rating
        Rating rating = new Rating();
        rating.setUser(user);
        rating.setPg(pg);
        rating.setStars(stars);
        rating.setReview(review);

        ratingRepository.save(rating);

        // ✅ Calculate average
        List<Rating> ratings = ratingRepository.findByPg(pg);

        double avg = ratings.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);

        pg.setRating(avg);
        pgRepository.save(pg);

        return "redirect:/my-bookings";
    }
 //rating page
    @GetMapping("/rate-page/{pgId}")
    public String ratePage(@PathVariable int pgId, Model model) {

        PG pg = pgRepository.findById(pgId).orElse(null);

        model.addAttribute("pg", pg);

        return "rate-page";
    }
    @GetMapping("/pg/{id}")
    public String viewPG(@PathVariable int id, Model model) {

        PG pg = pgRepository.findById(id).orElse(null);

        List<Rating> reviews = ratingRepository.findByPg(pg);

        model.addAttribute("pg", pg);
        model.addAttribute("reviews", reviews);

        return "pg-view";
    }
    //Wishlist

    @GetMapping("/wishlist")
    public String showWishlist(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if(userId == null){
            return "redirect:/login";
        }
        int count = wishlistService.getWishlistCount(Long.valueOf(userId));
        model.addAttribute("wishlistCount", count);

        List<Wishlist> wishlist = wishlistService.getWishlist(Long.valueOf(userId));

        List<Map<String, Object>> data = new ArrayList<>();

        for(Wishlist w : wishlist){

            PG pg = pgRepository.findById(Math.toIntExact(w.getPgId())).orElse(null);

            if(pg != null){
                Map<String, Object> map = new HashMap<>();
                map.put("pg", pg);
                data.add(map);
            }
        }

        model.addAttribute("data", data);

        return "wishlist";
    }
    @PostMapping("/wishlist/toggle/{pgId}")
    @ResponseBody
    public String toggleWishlist(@PathVariable Long pgId, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if(userId == null){
            return "error";
        }

        return wishlistService.toggleWishlist(Long.valueOf(userId), pgId);
    }
    //After booked from wishlist
    @GetMapping("/wishlist/remove/{pgId}")
    public String removeFromWishlist(@PathVariable Long pgId, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        if(userId == null){
            return "redirect:/login";
        }

        wishlistService.remove(Long.valueOf(userId), pgId);

        return "redirect:/wishlist";
    }
    @GetMapping("/wishlist/count")
    @ResponseBody
    public ResponseEntity<Integer> getWishlistCount(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        int count = 0;
        if(userId != null){
            count = wishlistService.getWishlistCount(Long.valueOf(userId));
        }

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")   // 🔥 VERY IMPORTANT
                .body(count);
    }
    //owner-dashboard
    @GetMapping("/owner/dashboard")
    public String ownerDashboard(Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if(userId == null || role == null || !role.equals("owner")) {
            return "redirect:/login";
        }

        // 🔹 MY PGs
        List<PG> allPGs = pgRepository.findAll();
        List<PG> myPGs = new ArrayList<>();

        for(PG pg : allPGs) {
            if(pg.getOwner() != null && pg.getOwner().getId() == userId) {
                myPGs.add(pg);
            }
        }


        // 🔹 BOOKINGS
        List<Booking> bookings = bookingRepository.findAll();
        List<Map<String, Object>> bookingData = new ArrayList<>();

        for(Booking b : bookings) {
            PG pg = b.getPg();

            if(pg != null && pg.getOwner() != null &&
                    pg.getOwner().getId() == userId) {

                Map<String, Object> map = new HashMap<>();
                map.put("pg", pg);
                map.put("user", b.getUser());
                map.put("booking", b);

                bookingData.add(map);
            }
        }
        long pending = bookingData.stream()
                .filter(m -> ((Booking)m.get("booking")).getStatus().equals("PENDING"))
                .count();

        model.addAttribute("pending", pending);

        // 🔹 STATS
        int total = myPGs.size();

        int confirmed = (int) bookingData.stream()
                .filter(m -> ((Booking)m.get("booking")).getStatus().equals("CONFIRMED"))
                .count();

        model.addAttribute("total", total);
        model.addAttribute("confirmed", confirmed);
        model.addAttribute("pgList", myPGs);
        model.addAttribute("bookings", bookingData);

        return "owner-dashboard";

    }
}
