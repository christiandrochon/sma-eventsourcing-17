package fr.cdrochon.smamonolithe.event.query.entities;

import fr.cdrochon.smamonolithe.event.query.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity @NoArgsConstructor @AllArgsConstructor @Builder
public class GarageQueryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant instant;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @ManyToOne
    private GarageQuery garageQuery;
}
