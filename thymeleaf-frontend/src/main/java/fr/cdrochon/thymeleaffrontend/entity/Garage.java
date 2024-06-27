package fr.cdrochon.thymeleaffrontend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String nomGarage;
    @Embedded
    private AdresseGarage adresseGarage;
    private String emailContactGarage;
}
