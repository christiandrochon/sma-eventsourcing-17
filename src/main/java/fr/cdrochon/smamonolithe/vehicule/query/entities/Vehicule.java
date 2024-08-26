package fr.cdrochon.smamonolithe.vehicule.query.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {
    
    @Id
    private String id;
    
    private String immatriculationVehicule;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateMiseEnCirculationVehicule;
    @Enumerated(EnumType.STRING)
    private VehiculeStatus vehiculeStatus;
    
    @OneToOne(mappedBy = "vehicule")
    @JsonBackReference("dossier-vehicule") //relation qui ne doit pas etre serialisée en json
    private Dossier dossier;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
    /**
     * Au lieu d'inclure l'objet Dossier complet dans le toString(), on inlue uniquement l'identifiant du Dossier. Cela évite la récursion infinie tout en
     * fournissant une information utile sur la relation
     *
     * @return String représentant l'objet Vehicule
     */
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                // Ne pas appeler dossier.toString() pour éviter la récursion
                ", dossierId=" + (dossier != null ? dossier.getId() : "null") +
                '}';
    }
}
