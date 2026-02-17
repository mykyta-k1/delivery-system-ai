package com.hackathon.delivery.courier.repository;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.model.TransportType;
import com.hackathon.delivery.shared.GeoPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Тести CourierRepository")
class CourierRepositoryTest {

    @Autowired
    private CourierRepository courierRepository;

    @BeforeEach
    void setUp() {
        courierRepository.deleteAll();
    }

    @Test
    @DisplayName("should find all couriers by status")
    void shouldFindAllCouriersByStatus() {
        Courier c1 = createCourier(CourierStatus.FREE);
        Courier c2 = createCourier(CourierStatus.BUSY);
        Courier c3 = createCourier(CourierStatus.FREE);
        courierRepository.saveAll(List.of(c1, c2, c3));

        List<Courier> freeCouriers = courierRepository.findAllByStatus(CourierStatus.FREE);

        assertThat(freeCouriers).hasSize(2)
                .extracting(Courier::getStatus)
                .containsOnly(CourierStatus.FREE);
    }

    @Test
    @DisplayName("should return empty list if no couriers with status")
    void shouldReturnEmptyIfNoCouriersWithStatus() {
        Courier c1 = createCourier(CourierStatus.BUSY);
        courierRepository.save(c1);

        List<Courier> freeCouriers = courierRepository.findAllByStatus(CourierStatus.FREE);

        assertThat(freeCouriers).isEmpty();
    }

    private Courier createCourier(CourierStatus status) {
        Courier courier = new Courier();
        courier.setStatus(status);
        courier.setTransportType(TransportType.WALKER);
        courier.setLocation(new GeoPoint(0, 0));
        return courier;
    }
}
