package com.hackathon.delivery.order.repository;

import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.shared.GeoPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Тести OrderRepository")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("should find orders by status")
    void shouldFindOrdersByStatus() {
        Order order1 = createOrder(OrderStatus.NEW);
        Order order2 = createOrder(OrderStatus.ASSIGNED);
        Order order3 = createOrder(OrderStatus.NEW);
        orderRepository.saveAll(List.of(order1, order2, order3));

        List<Order> newOrders = orderRepository.findByStatus(OrderStatus.NEW);

        assertThat(newOrders).hasSize(2)
                .extracting(Order::getStatus)
                .containsOnly(OrderStatus.NEW);
    }

    @Test
    @DisplayName("should return empty list if no orders with status")
    void shouldReturnEmptyIfNoOrdersWithStatus() {
        Order order1 = createOrder(OrderStatus.ASSIGNED);
        orderRepository.save(order1);

        List<Order> newOrders = orderRepository.findByStatus(OrderStatus.NEW);

        assertThat(newOrders).isEmpty();
    }

    @Test
    @DisplayName("should find oldest NEW order")
    void shouldFindOldestNewOrder() {
        Order order1 = createOrder(OrderStatus.NEW);
        order1.setCreatedAt(LocalDateTime.now().minusHours(2));

        Order order2 = createOrder(OrderStatus.NEW);
        order2.setCreatedAt(LocalDateTime.now().minusHours(1));

        Order order3 = createOrder(OrderStatus.NEW);
        order3.setCreatedAt(LocalDateTime.now());

        orderRepository.saveAll(List.of(order2, order1, order3)); // Saved in mixed order

        Order oldest = orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW);

        assertThat(oldest).isNotNull();
        assertThat(oldest.getId()).isEqualTo(order1.getId());
    }

    @Test
    @DisplayName("should return null if no NEW order for oldest search")
    void shouldReturnNullIfNoNewOrder() {
        Order order1 = createOrder(OrderStatus.ASSIGNED);
        orderRepository.save(order1);

        Order oldest = orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW);

        assertThat(oldest).isNull();
    }

    private Order createOrder(OrderStatus status) {
        Order order = new Order();
        order.setStatus(status);
        order.setSource(new GeoPoint(0, 0));
        order.setDestination(new GeoPoint(10, 10));
        order.setWeight(BigDecimal.ONE);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
