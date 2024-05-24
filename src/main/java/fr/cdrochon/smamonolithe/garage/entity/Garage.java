package fr.cdrochon.smamonolithe.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor @Builder
public class Garage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomGarage;
    @Embedded
    private AdresseGarage adresseGarage;
//    @Embedded
//    private ResponsableGarage responsableGarage;
    private String emailContactGarage;
}
