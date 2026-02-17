package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.factory.CourierFactory;
import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.repository.CourierRepository;
import com.hackathon.delivery.order.factory.OrderFactory;
import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.shared.GeoPoint;
import com.hackathon.delivery.shared.exception.NoCouriersAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DispatchService Tests")
class DispatchServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private DispatchService dispatchService;

    @Test
    @DisplayName("Should assign nearest free courier to order")
    void shouldAssignNearestCourier() {
        // Given
        GeoPoint orderSource = new GeoPoint(10, 10);
        Order order = OrderFactory.createNewOrder(orderSource, new GeoPoint(50, 50));

        UUID nearCourierId = UUID.randomUUID();
        Courier nearCourier = CourierFactory.createCourierWithId(nearCourierId, CourierStatus.FREE, 12, 12);
        Courier farCourier = CourierFactory.createCourierWithId(UUID.randomUUID(), CourierStatus.FREE, 30, 30);

        List<Courier> freeCouriers = Arrays.asList(nearCourier, farCourier);

        when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(freeCouriers);
        when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Courier assignedCourier = dispatchService.assignCourier(order);

        // Then
        assertThat(assignedCourier).isNotNull();
        assertThat(assignedCourier.getId()).isEqualTo(nearCourierId);
        assertThat(assignedCourier.getStatus()).isEqualTo(CourierStatus.BUSY);

        verify(courierRepository).findAllByStatus(CourierStatus.FREE);
        verify(courierRepository).save(nearCourier);
    }

    @Test
    @DisplayName("Should throw exception when no free couriers available")
    void shouldThrowExceptionWhenNoCouriersAvailable() {
        // Given
        Order order = OrderFactory.createNewOrder(new GeoPoint(10, 10), new GeoPoint(50, 50));

        when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> dispatchService.assignCourier(order))
                .isInstanceOf(NoCouriersAvailableException.class)
                .hasMessageContaining("No free couriers available");

        verify(courierRepository).findAllByStatus(CourierStatus.FREE);
        verify(courierRepository, never()).save(any(Courier.class));
    }
}
