package fr.cdrochon.smamonolithe.garage.query.entities;

import fr.cdrochon.smamonolithe.garage.query.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity @NoArgsConstructor @AllArgsConstructor @Builder
public class GarageTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Instant instant;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @ManyToOne
    private Garage garageQuery;
}
