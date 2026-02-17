package com.hackathon.delivery.order.model;

import com.hackathon.delivery.shared.GeoPoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Embedded
        @AttributeOverrides({
                        @AttributeOverride(name = "x", column = @Column(name = "source_x")),
                        @AttributeOverride(name = "y", column = @Column(name = "source_y"))
        })
        private GeoPoint source;

        @Embedded
        @AttributeOverrides({
                        @AttributeOverride(name = "x", column = @Column(name = "dest_x")),
                        @AttributeOverride(name = "y", column = @Column(name = "dest_y"))
        })
        private GeoPoint destination;

        @Enumerated(EnumType.STRING)
        private OrderStatus status;

        private UUID courierId;

        private BigDecimal weight = BigDecimal.ONE;

        private LocalDateTime createdAt = LocalDateTime.now();
}
