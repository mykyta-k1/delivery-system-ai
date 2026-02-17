package com.hackathon.delivery.order.repository;

import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}
