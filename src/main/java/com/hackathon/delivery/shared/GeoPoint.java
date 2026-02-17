package com.hackathon.delivery.shared;

import jakarta.persistence.Embeddable;

@Embeddable
public record GeoPoint(int x, int y) {
    public GeoPoint {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must be non-negative");
        }
    }
}
