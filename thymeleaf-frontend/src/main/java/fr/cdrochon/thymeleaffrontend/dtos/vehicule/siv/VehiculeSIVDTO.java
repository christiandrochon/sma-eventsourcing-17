package fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
//@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeSIVDTO {

    private String immatriculation;
    private String dateDeMiseEnCirculation;
    private String dateValiditeControleTechnique;
    private String modele;
    private String marque;
    private String typeCarburant;
    private boolean climatisation;
}
