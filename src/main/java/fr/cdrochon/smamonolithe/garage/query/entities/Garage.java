package fr.cdrochon.smamonolithe.garage.query.entities;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import lombok.*;

import javax.persistence.*;

/**
 * En lien avec les entités Garage dans l'autre package
 * <p>
 * Cette partie query est un microservice à part entiere, sur la base de lecture
 * Je recupere ces données sous forme d'events
 */
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class Garage {
    @Id
    private String idQuery;
    private String nomGarage;
    private String mailResponsable;
    @Embedded
    private AdresseGarage adresseGarage;
    //    @Embedded
    //    private ResponsableGarage responsableGarage;
    
    @Enumerated
    private GarageStatus garageStatus;
//    @OneToMany(mappedBy = "garageQuery")
//    List<GarageQueryTransaction> garageQueryTransactions;
//    private Instant dateQuery;


}

