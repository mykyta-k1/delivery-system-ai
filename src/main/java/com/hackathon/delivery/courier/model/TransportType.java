package com.hackathon.delivery.courier.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    /** Піший кур'єр, до 5 кг. */
    WALKER(BigDecimal.valueOf(5)),
    /** Велокур'єр, до 15 кг. */
    BICYCLE(BigDecimal.valueOf(15)),
    /** Кур'єр на авто, до 50 кг. */
    CAR(BigDecimal.valueOf(50));

    private final BigDecimal capacity;
}
