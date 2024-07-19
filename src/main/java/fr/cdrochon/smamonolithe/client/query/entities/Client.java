package fr.cdrochon.smamonolithe.client.query.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.smamonolithe.client.command.enums.ClientStatus;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import lombok.*;

import javax.persistence.*;


@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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
    
    //relation qui ne doit pas etre serialisée
    @OneToOne(mappedBy = "client")
    @JsonBackReference
//    @JsonIgnore
    private Dossier dossier;
    
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
