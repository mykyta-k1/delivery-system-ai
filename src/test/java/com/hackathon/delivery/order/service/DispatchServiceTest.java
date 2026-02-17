package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.factory.CourierFactory;
import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.model.TransportType;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести DispatchService")
class DispatchServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private DispatchService dispatchService;

    @Test
    @DisplayName("Повинен призначити найближчого кур'єра, що підходить за вагою")
    void shouldAssignNearestCapableCourier() {
        // Given
        Order order = OrderFactory.createNewOrder(
                new GeoPoint(0, 0),
                new GeoPoint(0, 0),
                BigDecimal.valueOf(10) // 10kg order
        );

        Courier courierA = CourierFactory.createFreeCourier(10, 0, TransportType.WALKER); // 1km away, cap 5kg
        Courier courierB = CourierFactory.createFreeCourier(50, 0, TransportType.BICYCLE); // 5km away, cap 15kg

        when(courierRepository.findAllByStatus(CourierStatus.FREE))
                .thenReturn(List.of(courierA, courierB));

        // When
        Courier assignedCourier = dispatchService.assignCourier(order);

        // Then
        assertThat(assignedCourier).isEqualTo(courierB);
        assertThat(assignedCourier.getStatus()).isEqualTo(CourierStatus.BUSY);
    }

    @Test
    @DisplayName("Повинен викинути виняток, якщо немає відповідного кур'єра (перевищення ваги)")
    void shouldThrowExceptionWhenNoCapableCourierAvailable() {
        // Given
        Order order = OrderFactory.createNewOrder(
                new GeoPoint(0, 0),
                new GeoPoint(0, 0),
                BigDecimal.valueOf(100) // 100kg order
        );

        Courier courierA = CourierFactory.createFreeCourier(10, 0, TransportType.WALKER);
        Courier courierB = CourierFactory.createFreeCourier(10, 0, TransportType.BICYCLE);
        Courier courierC = CourierFactory.createFreeCourier(10, 0, TransportType.CAR);

        when(courierRepository.findAllByStatus(CourierStatus.FREE))
                .thenReturn(List.of(courierA, courierB, courierC));

        // When/Then
        assertThatThrownBy(() -> dispatchService.assignCourier(order))
                .isInstanceOf(NoCouriersAvailableException.class)
                .hasMessage("No free couriers available for assignment");
    }

    @Test
    @DisplayName("Повинен призначити найближчого серед рівних")
    void shouldAssignNearestAmongEquals() {
        // Given
        Order order = OrderFactory.createNewOrder(
                new GeoPoint(0, 0),
                new GeoPoint(0, 0),
                BigDecimal.valueOf(1) // 1kg order
        );

        Courier courierA = CourierFactory.createFreeCourier(20, 0, TransportType.WALKER); // 2km away
        Courier courierB = CourierFactory.createFreeCourier(100, 0, TransportType.WALKER); // 10km away

        when(courierRepository.findAllByStatus(CourierStatus.FREE))
                .thenReturn(List.of(courierA, courierB));

        // When
        Courier assignedCourier = dispatchService.assignCourier(order);

        // Then
        assertThat(assignedCourier).isEqualTo(courierA);
    }

    @Test
    @DisplayName("Повинен викинути виняток, якщо взагалі немає доступних кур'єрів")
    void shouldThrowExceptionWhenNoCouriersAvailable() {
        // Given
        Order order = OrderFactory.createNewOrder(new GeoPoint(0, 0), new GeoPoint(10, 10));
        when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(Collections.emptyList());

        // When/Then
        assertThatThrownBy(() -> dispatchService.assignCourier(order))
                .isInstanceOf(NoCouriersAvailableException.class)
                .hasMessage("No free couriers available for assignment");
    }
}
