package fr.cdrochon.thymeleaffrontend.entity.garage;

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
    private String emailContactGarage;
    @Embedded
    private AdresseGarage adresse;

}
