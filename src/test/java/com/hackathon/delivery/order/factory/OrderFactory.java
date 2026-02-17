package com.hackathon.delivery.order.factory;

import com.hackathon.delivery.order.model.Order;
import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.shared.GeoPoint;
import org.instancio.Instancio;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.instancio.Select.field;

public class OrderFactory {

    public static Order createNewOrder(GeoPoint source, GeoPoint destination) {
        return Instancio.of(Order.class)
                .set(field(Order::getSource), source)
                .set(field(Order::getDestination), destination)
                .set(field(Order::getStatus), OrderStatus.NEW)
                .set(field(Order::getCreatedAt), LocalDateTime.now())
                .ignore(field(Order::getId))
                .ignore(field(Order::getCourierId))
                .create();
    }

    public static Order createAssignedOrder(GeoPoint source, GeoPoint destination, UUID courierId) {
        return Instancio.of(Order.class)
                .set(field(Order::getSource), source)
                .set(field(Order::getDestination), destination)
                .set(field(Order::getStatus), OrderStatus.ASSIGNED)
                .set(field(Order::getCourierId), courierId)
                .set(field(Order::getCreatedAt), LocalDateTime.now())
                .ignore(field(Order::getId))
                .create();
    }
}
