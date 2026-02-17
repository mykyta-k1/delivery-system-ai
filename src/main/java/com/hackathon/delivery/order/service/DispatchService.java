package com.hackathon.delivery.order.service;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.repository.CourierRepository;
import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.shared.GeoPoint;
import com.hackathon.delivery.shared.exception.NoCouriersAvailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final CourierRepository courierRepository;

    @Transactional
    public Courier assignCourier(Order order) {
        List<Courier> freeCouriers = courierRepository.findAllByStatus(CourierStatus.FREE);

        if (freeCouriers.isEmpty()) {
            throw new NoCouriersAvailableException("No free couriers available for assignment");
        }

        Courier nearestCourier = findNearestCourier(freeCouriers, order.getSource());
        nearestCourier.setStatus(CourierStatus.BUSY);
        courierRepository.save(nearestCourier);

        return nearestCourier;
    }

    private Courier findNearestCourier(List<Courier> couriers, GeoPoint orderSource) {
        return couriers.stream()
                .min((c1, c2) -> Double.compare(
                        calculateDistance(c1.getLocation(), orderSource),
                        calculateDistance(c2.getLocation(), orderSource)))
                .orElseThrow(() -> new NoCouriersAvailableException("No couriers found"));
    }

    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        int dx = p2.x() - p1.x();
        int dy = p2.y() - p1.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
