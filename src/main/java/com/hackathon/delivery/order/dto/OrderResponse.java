package com.hackathon.delivery.order.dto;

import com.hackathon.delivery.order.model.OrderStatus;
import com.hackathon.delivery.shared.GeoPoint;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        GeoPoint source,
        GeoPoint destination,
        OrderStatus status,
        UUID courierId) {
}
