package fr.cdrochon.smamonolithe.event.query.entities;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * En lien avec les entités Garage dans l'autre package
 *
 * Cette partie query est un microservice à part entiere, sur la base de lecture
 * Je recupere ces données sous forme d'events
 */
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class GarageQuery {
    @Id
    private String idQuery;
    private String nomGarage;
    private String mailResponsable;
    @Enumerated
    private GarageStatus garageStatus;
//    @OneToMany(mappedBy = "garageQuery")
//    List<GarageQueryTransaction> garageQueryTransactions;
//    private Instant dateQuery;


}

