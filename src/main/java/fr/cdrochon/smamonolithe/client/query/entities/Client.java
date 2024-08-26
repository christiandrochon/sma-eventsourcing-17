package fr.cdrochon.smamonolithe.client.query.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.*;

import javax.persistence.*;


@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Client {
    
    @Id
    private String id;
    
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    
    @Embedded
    private AdresseClient adresse;
    @Enumerated
    private ClientStatus clientStatus;
    
    @OneToOne(mappedBy = "client")
    @JsonBackReference("dossier-client") //relation qui ne doit pas etre serialisée
    private Dossier dossier;
    
    @ManyToOne
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;
    
    /**
     * Au lieu d'inclure l'objet Dossier complet dans le toString(), on inclut uniquement l'identifiant du Dossier. Cela évite la récursion infinie tout en
     * fournissant une information utile sur la relation
     *
     * @return String représentant l'objet Client
     */
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                // Ne pas appeler dossier.toString() pour éviter la récursion
                ", dossierId=" + (dossier != null ? dossier.getId() : "null") +
                '}';
    }
}
