package com.hackathon.delivery.order.controller;

import com.hackathon.delivery.order.dto.CreateOrderRequest;
import com.hackathon.delivery.order.dto.OrderResponse;
import com.hackathon.delivery.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Створити нове замовлення", description = "Створює замовлення та призначає найближчого кур'єра")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    @Operation(summary = "Отримати замовлення", description = "Отримати список всіх замовлень або відфільтрувати за статусом")
    public java.util.List<OrderResponse> getOrders(
            @RequestParam(required = false) com.hackathon.delivery.order.model.OrderStatus status) {
        return orderService.getOrders(status);
    }

    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Завершити замовлення", description = "Завершує замовлення та звільняє кур'єра")
    public void completeOrder(@PathVariable java.util.UUID id) {
        orderService.completeOrder(id);
    }
}
