package com.hackathon.delivery.courier.repository;

import com.hackathon.delivery.courier.model.Courier;
import com.hackathon.delivery.courier.model.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
    List<Courier> findAllByStatus(CourierStatus status);
}
