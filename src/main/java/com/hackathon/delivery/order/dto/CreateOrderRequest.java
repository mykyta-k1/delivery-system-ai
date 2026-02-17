package com.hackathon.delivery.order.dto;

import com.hackathon.delivery.shared.GeoPoint;
import java.math.BigDecimal;

public record CreateOrderRequest(
                GeoPoint source,
                GeoPoint destination,
                BigDecimal weight) {
}
