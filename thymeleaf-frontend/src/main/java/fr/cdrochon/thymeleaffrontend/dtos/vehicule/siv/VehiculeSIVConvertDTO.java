package fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeSIVConvertDTO {
    

    private String immatriculation;
    private Instant dateDeMiseEnCirculation;
    private Instant dateValiditeControleTechnique;
    private String modele;
    private MarqueVehiculeDTO marque;
    private TypeCarburantDTO typeCarburant;
    private boolean climatisation;
    
}
