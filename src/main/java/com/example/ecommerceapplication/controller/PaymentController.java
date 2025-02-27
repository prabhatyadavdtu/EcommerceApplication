package com.example.ecommerceapplication.controller;

import com.example.ecommerceapplication.model.Order;
import com.example.ecommerceapplication.model.PaymentDetails;
import com.example.ecommerceapplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public void processPayment(@RequestBody PaymentDetails paymentDetails, @RequestParam Long orderId) {
        Order order = new Order(); // Fetch order by ID (omitted for brevity)
        paymentService.processPayment(order, paymentDetails);
    }
}
