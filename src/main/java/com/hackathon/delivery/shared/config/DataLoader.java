package com.hackathon.delivery.shared.config;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import com.hackathon.delivery.courier.repository.CourierRepository;
import com.hackathon.delivery.shared.GeoPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final CourierRepository courierRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting data seeding...");

        Random random = new Random();
        int courierCount = 10;

        for (int i = 0; i < courierCount; i++) {
            var courier = new Courier();
            courier.setStatus(CourierStatus.FREE);
            courier.setLocation(new GeoPoint(
                    random.nextInt(100), // x: 0-99
                    random.nextInt(100) // y: 0-99
            ));
            courierRepository.save(courier);
        }

        log.info("✅ Test Data Loaded: {} couriers created", courierCount);
    }
}
