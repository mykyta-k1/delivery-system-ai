package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.order.dto.CreateOrderRequest;
import com.hackathon.delivery.order.dto.OrderResponse;
import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DispatchService dispatchService;

    /**
     * Створює нове замовлення та автоматично призначає кур'єра.
     *
     * @param request DTO з даними для створення замовлення (точки А і Б, вага).
     * @return DTO створеного замовлення з інформацією про призначеного кур'єра.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Create new order
        var order = new Order();
        order.setSource(request.source());
        order.setDestination(request.destination());
        order.setWeight(request.weight());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());

        // Assign courier
        Courier assignedCourier = dispatchService.assignCourier(order);

        // Update order with courier assignment
        order.setCourierId(assignedCourier.getId());
        order.setStatus(OrderStatus.ASSIGNED);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Map to DTO
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getSource(),
                savedOrder.getDestination(),
                savedOrder.getStatus(),
                savedOrder.getCourierId());
    }
}
