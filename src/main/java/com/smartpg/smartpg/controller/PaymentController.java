package com.smartpg.smartpg.controller;

import com.smartpg.smartpg.model.Booking;
import com.smartpg.smartpg.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {

    @Autowired
    private BookingRepository bookingRepository;

    // 🔹 Show Payment Page
    @GetMapping("/payment/{id}")
    public String showPayment(@PathVariable int id, Model model) {

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "redirect:/home";
        }

        // ⚠️ You don't have amount → use PG price
        model.addAttribute("booking", booking);
        model.addAttribute("amount", booking.getPg().getPrice());

        return "payment";
    }

    // 🔹 Process Payment
    @PostMapping("/pay/{id}")
    public String processPayment(@PathVariable int id,
                                 @RequestParam(required = false) String paymentMethod,
                                 @RequestParam(required = false) String upi,
                                 @RequestParam(required = false) String cardNumber) {

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "redirect:/home";
        }

        // 🔹 UPI validation
        if ("upi".equals(paymentMethod)) {
            if (upi == null || !upi.contains("@")) {
                return "redirect:/payment/" + id;
            }
        }

        // 🔹 CARD validation
        if ("card".equals(paymentMethod)) {
            if (cardNumber == null || cardNumber.length() < 12) {
                return "redirect:/payment/" + id;
            }
        }

        // 🔹 QR (no validation needed)
        if ("qr".equals(paymentMethod)) {
            // always allow
        }

        // ✅ SUCCESS
        booking.setPaymentStatus("SUCCESS");
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentId("PAY" + System.currentTimeMillis());

        bookingRepository.save(booking);

        return "redirect:/payment-success";
    }

    // 🔹 Success Page
    @GetMapping("/payment-success")
    public String success() {
        return "payment-success";
    }
}