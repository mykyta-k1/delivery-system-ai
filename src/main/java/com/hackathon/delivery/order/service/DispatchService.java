package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.repository.CourierRepository;
import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.order.repository.OrderRepository;
import com.hackathon.delivery.shared.GeoPoint;
import com.hackathon.delivery.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    /**
     * Призначає найкращого вільного кур'єра, враховуючи вагу та справедливість.
     * Якщо кур'єра не знайдено, повертає null (замовлення стає в чергу).
     *
     * @param order Замовлення, для якого потрібно знайти кур'єра.
     * @return Призначений кур'єр або null.
     */
    @Transactional
    public Courier assignCourier(Order order) {
        List<Courier> freeCouriers = courierRepository.findAllByStatus(CourierStatus.FREE);

        // 1. Filter by capacity
        List<Courier> capableCouriers = freeCouriers.stream()
                .filter(courier -> courier.getTransportType().getCapacity().compareTo(order.getWeight()) >= 0)
                .toList();

        if (capableCouriers.isEmpty()) {
            return null; // Queue logic: order remains NEW
        }

        // 2. Fairness Logic: Find best courier
        Courier bestCourier = capableCouriers.stream()
                .min(getFairnessComparator(order.getSource()))
                .orElse(null);

        if (bestCourier != null) {
            assignAndSave(bestCourier, order);
        }

        return bestCourier;
    }

    /**
     * Завершує замовлення, звільняє кур'єра та пробує призначити йому нове
     * замовлення з черги.
     *
     * @param orderId ID замовлення.
     */
    @Transactional
    public void completeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        if (order.getCourierId() != null) {
            Courier courier = courierRepository.findById(order.getCourierId())
                    .orElseThrow(() -> new EntityNotFoundException("Courier not found: " + order.getCourierId()));

            courier.setStatus(CourierStatus.FREE);
            courier.setCompletedOrders(courier.getCompletedOrders() + 1);
            courierRepository.save(courier);

            // Trigger: Try to assign oldest queued order using full fairness logic
            triggerAutoDispatch();
        }
    }

    private void triggerAutoDispatch() {
        Order oldestOrder = orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.NEW);

        if (oldestOrder != null) {
            assignCourier(oldestOrder);
        }
    }

    private void assignAndSave(Courier courier, Order order) {
        courier.setStatus(CourierStatus.BUSY);
        courierRepository.save(courier);

        order.setCourierId(courier.getId());
        order.setStatus(OrderStatus.ASSIGNED);
        orderRepository.save(order);
    }

    private Comparator<Courier> getFairnessComparator(GeoPoint orderSource) {
        return (c1, c2) -> {
            double dist1 = calculateDistance(c1.getLocation(), orderSource);
            double dist2 = calculateDistance(c2.getLocation(), orderSource);

            if (Math.abs(dist1 - dist2) < 1.0) {
                // Priority to fewer completed orders
                return Integer.compare(c1.getCompletedOrders(), c2.getCompletedOrders());
            } else {
                // Priority to shorter distance
                return Double.compare(dist1, dist2);
            }
        };
    }

    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        int dx = p2.x() - p1.x();
        int dy = p2.y() - p1.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
