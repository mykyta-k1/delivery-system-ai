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

        if (assignedCourier != null) {
            order.setCourierId(assignedCourier.getId());
            order.setStatus(OrderStatus.ASSIGNED);
        }
        // Else: Order remains NEW (Queued)

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Map to DTO
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderResponse> getOrders(OrderStatus status) {
        java.util.List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Завершує замовлення.
     *
     * @param id ID замовлення.
     */
    @Transactional
    public void completeOrder(java.util.UUID id) {
        dispatchService.completeOrder(id);
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getSource(),
                order.getDestination(),
                order.getStatus(),
                order.getCourierId());
    }
}
