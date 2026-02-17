package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.factory.CourierFactory;
import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.model.TransportType;
import com.hackathon.delivery.courier.repository.CourierRepository;
import com.hackathon.delivery.order.factory.OrderFactory;
import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.order.repository.OrderRepository;
import com.hackathon.delivery.shared.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести DispatchService")
class DispatchServiceTest {

        @Mock
        private CourierRepository courierRepository;

        @Mock
        private OrderRepository orderRepository;

        @InjectMocks
        private DispatchService dispatchService;

        @Test
        @DisplayName("Повинен призначити найближчого кур'єра, що підходить за вагою")
        void shouldAssignNearestCapableCourier() {
                // Given
                Order order = OrderFactory.createNewOrder(new GeoPoint(0, 0), new GeoPoint(0, 0),
                                BigDecimal.valueOf(10));

                Courier courierA = CourierFactory.createFreeCourier(10, 0, TransportType.WALKER); // 1km, cap 5kg (not
                                                                                                  // capable)
                Courier courierB = CourierFactory.createFreeCourier(50, 0, TransportType.BICYCLE); // 5km, cap 15kg
                                                                                                   // (capable)

                when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(List.of(courierA, courierB));

                // When
                Courier assignedCourier = dispatchService.assignCourier(order);

                // Then
                assertThat(assignedCourier).isEqualTo(courierB);
                verify(courierRepository).save(courierB);
        }

        @Test
        @DisplayName("Повинен повернути null (черга), якщо немає здатного кур'єра")
        void shouldReturnNullWhenNoCapableCourier() {
                // Given
                Order order = OrderFactory.createNewOrder(new GeoPoint(0, 0), new GeoPoint(0, 0),
                                BigDecimal.valueOf(100)); // Heavy

                Courier courierA = CourierFactory.createFreeCourier(10, 0, TransportType.WALKER);

                when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(List.of(courierA));

                // When
                Courier assignedCourier = dispatchService.assignCourier(order);

                // Then
                assertThat(assignedCourier).isNull();
                verify(courierRepository, never()).save(any(Courier.class));
        }

        @Test
        @DisplayName("Повинен призначити кур'єра з меншою кількістю замовлень, якщо відстань схожа (<1км)")
        void shouldAssignFairlyWhenDistancesAreSimilar() {
                // Given
                GeoPoint orderSource = new GeoPoint(0, 0);
                Order order = OrderFactory.createNewOrder(orderSource, new GeoPoint(10, 10), BigDecimal.ONE);

                // Courier A: (10,0) -> dist 10.0, 5 completed orders
                Courier courierA = CourierFactory.createFreeCourier(10, 0, TransportType.CAR);
                courierA.setCompletedOrders(5);

                // Courier B: (10,1) -> dist 10.049 (< 10.0 + 1.0), 0 completed orders
                Courier courierB = CourierFactory.createFreeCourier(10, 1, TransportType.CAR);
                courierB.setCompletedOrders(0);

                when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(List.of(courierA, courierB));

                // When
                Courier assignedCourier = dispatchService.assignCourier(order);

                // Then
                // Distance diff is 0.5km (< 1.0), so we pick B because 0 < 5 completed orders
                assertThat(assignedCourier).isEqualTo(courierB);
        }

        @Test
        @DisplayName("Повинен призначити найближчого кур'єра, якщо різниця відстаней значна (>=1км)")
        void shouldAssignNearestWhenDistancesAreDistinct() {
                // Given
                GeoPoint orderSource = new GeoPoint(0, 0);
                Order order = OrderFactory.createNewOrder(orderSource, new GeoPoint(10, 10), BigDecimal.ONE);

                // Courier A: 10km away, 5 completed orders
                Courier courierA = CourierFactory.createFreeCourier(100, 0, TransportType.CAR);
                courierA.setCompletedOrders(5);

                // Courier B: 12km away, 0 completed orders
                Courier courierB = CourierFactory.createFreeCourier(120, 0, TransportType.CAR);
                courierB.setCompletedOrders(0);

                when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(List.of(courierA, courierB));

                // When
                Courier assignedCourier = dispatchService.assignCourier(order);

                // Then
                // Distance diff is 2.0km (>= 1.0), so we pick A (nearest) despite more orders
                assertThat(assignedCourier).isEqualTo(courierA);
        }

        @Test
        @DisplayName("Авто-призначення: Кур'єр завершує замовлення і отримує нове з черги через assignCourier")
        void shouldTriggerAutoDispatchOnOrderCompletion() {
                // Given
                UUID orderId = UUID.randomUUID();
                UUID courierId = UUID.randomUUID();

                Order completedOrder = new Order();
                completedOrder.setId(orderId);
                completedOrder.setCourierId(courierId);
                completedOrder.setStatus(OrderStatus.ASSIGNED);

                Courier courier = new Courier();
                courier.setId(courierId);
                courier.setStatus(CourierStatus.BUSY);
                courier.setTransportType(TransportType.CAR);
                courier.setCompletedOrders(0);

                Order queuedOrder = OrderFactory.createNewOrder(new GeoPoint(10, 10), new GeoPoint(20, 20),
                                BigDecimal.valueOf(5));
                queuedOrder.setStatus(OrderStatus.NEW);
                queuedOrder.setWeight(BigDecimal.valueOf(5));

                when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));
                when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
                when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW)).thenReturn(queuedOrder);

                // When we call finding free couriers, we expect our JUST freed courier to be
                // there
                // We need to mock findAllByStatus to return our courier (and maybe others)
                when(courierRepository.findAllByStatus(CourierStatus.FREE)).thenReturn(List.of(courier));

                // When
                dispatchService.completeOrder(orderId);

                // Then
                // 1. Order completed
                assertThat(completedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);

                // 2. Courier updated to FREE before auto-dispatch logic, then BUSY
                // Since the courier object is mutable and passed by reference, Mockito sees the
                // final state (BUSY)
                // So we verify it was saved twice (once for FREE, once for BUSY)
                verify(courierRepository, times(2)).save(courier);

                // 3. Queued order assigned (via assignCourier logic)
                assertThat(queuedOrder.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
                assertThat(queuedOrder.getCourierId()).isEqualTo(courierId);

                verify(orderRepository).save(completedOrder);
                verify(orderRepository).save(queuedOrder);
        }

        @Test
        @DisplayName("Авто-призначення: Не повинно нічого робитися, якщо немає замовлень в черзі")
        void shouldNotTriggerAutoDispatchIfNoQueuedOrders() {
                // Given
                UUID orderId = UUID.randomUUID();
                UUID courierId = UUID.randomUUID();

                Order completedOrder = new Order();
                completedOrder.setId(orderId);
                completedOrder.setCourierId(courierId);
                completedOrder.setStatus(OrderStatus.ASSIGNED);

                Courier courier = new Courier();
                courier.setId(courierId);
                courier.setStatus(CourierStatus.BUSY);

                when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));
                when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));
                when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW)).thenReturn(null);

                // When
                dispatchService.completeOrder(orderId);

                // Then
                verify(orderRepository).findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW);
                // No interaction with assignCourier logic (which would call findAllByStatus)
                verify(courierRepository, never()).findAllByStatus(any());
        }
}
