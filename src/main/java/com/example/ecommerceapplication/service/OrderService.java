package com.example.ecommerceapplication.service;

import com.example.ecommerceapplication.exception.ResourceNotFoundException;
import com.example.ecommerceapplication.model.Cart;
import com.example.ecommerceapplication.model.Order;
import com.example.ecommerceapplication.model.OrderStatus;
import com.example.ecommerceapplication.model.PaymentDetails;
import com.example.ecommerceapplication.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private PaymentService paymentService;

    public Order createOrder(Long userId, PaymentDetails paymentDetails) {
        Cart cart = cartService.getCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setItems(new ArrayList<>(cart.getItems()));
        order.setTotalAmount(cart.getTotalAmount());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Process payment
        paymentService.processPayment(order, paymentDetails);

        // Clear cart after successful order
        cartService.clearCart(userId);

        return orderRepository.save(order);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
