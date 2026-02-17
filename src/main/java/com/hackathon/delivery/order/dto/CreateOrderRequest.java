package com.hackathon.delivery.order.dto;

import com.hackathon.delivery.shared.GeoPoint;

public record CreateOrderRequest(
        GeoPoint source,
        GeoPoint destination) {
}
