package fr.cdrochon.smamonolithe.vehicule.query.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehiculeQueryDTO {
    
    private String id;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private VehiculeStatus vehiculeStatus;
    private ClientQueryDTO client;
    
}
