package fr.cdrochon.smamonolithe.garage.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;


@Entity
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor @Builder
public class Garage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomGarage;
//    @Embedded
//    private AdresseGarage adresseGarage;
//    @Embedded
//    private ResponsableGarage responsableGarage;
    private String emailContactGarage;
    private Instant dateQuery;
}
