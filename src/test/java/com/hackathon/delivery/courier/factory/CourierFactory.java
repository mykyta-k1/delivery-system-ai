package com.hackathon.delivery.courier.factory;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.model.TransportType;
import com.hackathon.delivery.shared.GeoPoint;
import org.instancio.Instancio;

import java.util.UUID;

import static org.instancio.Select.field;

public class CourierFactory {

    public static Courier createFreeCourier(int x, int y, TransportType transportType) {
        return Instancio.of(Courier.class)
                .set(field(Courier::getStatus), CourierStatus.FREE)
                .set(field(Courier::getTransportType), transportType)
                .set(field(Courier::getLocation), new GeoPoint(x, y))
                .create();
    }

    public static Courier createFreeCourier(int x, int y) {
        return createFreeCourier(x, y, TransportType.WALKER);
    }

    public static Courier createBusyCourier(int x, int y) {
        return Instancio.of(Courier.class)
                .set(field(Courier::getStatus), CourierStatus.BUSY)
                .set(field(Courier::getLocation), new GeoPoint(x, y))
                .create();
    }

    public static Courier createCourierWithId(UUID id, CourierStatus status, int x, int y) {
        return Instancio.of(Courier.class)
                .set(field(Courier::getId), id)
                .set(field(Courier::getStatus), status)
                .set(field(Courier::getLocation), new GeoPoint(x, y))
                .create();
    }
}
