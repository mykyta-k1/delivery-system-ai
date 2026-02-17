package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.factory.CourierFactory;
import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.order.dto.CreateOrderRequest;
import com.hackathon.delivery.order.dto.OrderResponse;
import com.hackathon.delivery.order.model.Order;

import java.math.BigDecimal;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.order.repository.OrderRepository;
import com.hackathon.delivery.shared.GeoPoint;
import com.hackathon.delivery.shared.exception.NoCouriersAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Повинен успішно створити замовлення та призначити кур'єра")
    void shouldCreateOrderSuccessfully() {
        // Given
        GeoPoint source = new GeoPoint(10, 10);
        GeoPoint destination = new GeoPoint(50, 50);
        CreateOrderRequest request = new CreateOrderRequest(source, destination, BigDecimal.ONE);

        UUID courierId = UUID.randomUUID();
        Courier assignedCourier = CourierFactory.createCourierWithId(courierId, CourierStatus.BUSY, 12, 12);

        UUID orderId = UUID.randomUUID();

        when(dispatchService.assignCourier(any(Order.class))).thenReturn(assignedCourier);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(orderId);
        assertThat(response.source()).isEqualTo(source);
        assertThat(response.destination()).isEqualTo(destination);
        assertThat(response.status()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(response.courierId()).isEqualTo(courierId);

        verify(dispatchService).assignCourier(any(Order.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Повинен викинути виняток, якщо під час створення замовлення немає доступних кур'єрів")
    void shouldThrowExceptionWhenNoCouriersAvailable() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                new GeoPoint(10, 10),
                new GeoPoint(50, 50),
                BigDecimal.ONE);

        when(dispatchService.assignCourier(any(Order.class)))
                .thenThrow(new NoCouriersAvailableException("No free couriers available"));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(NoCouriersAvailableException.class)
                .hasMessageContaining("No free couriers available");

        verify(dispatchService).assignCourier(any(Order.class));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
