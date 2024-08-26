package fr.cdrochon.smamonolithe.dossier.query.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Dossier {
    
    @Id
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    
    @OneToOne
    @JoinColumn(name = "client_id")
    @JsonManagedReference("dossier-client") //relation qui doit etre serialisée en json
    private Client client;
    
    @OneToOne
    @JoinColumn(name = "vehicule_id")
    @JsonManagedReference("dossier-vehicule") //relation qui doit etre serialisée en json
    private Vehicule vehicule;
    @Enumerated
    private DossierStatus dossierStatus;

    
    /**
     * Au lieu d'inclure l'objet Dossier complet dans le toString(), on inclut uniquement l'identifiant du Dossier. Cela évite la récursion infinie tout en
     * fournissant une information utile sur la relation
     *
     * @return String représentant l'objet Dossier
     */
    @Override
    public String toString() {
        return "Dossier{" +
                "id=" + id +
                ", client=" + (client != null ? "Client[id=" + client.getId() + "]" : "null") +
                ", vehicule=" + (vehicule != null ? "Vehicule[id=" + vehicule.getId() + "]" : "null") +
                '}';
    }
}
