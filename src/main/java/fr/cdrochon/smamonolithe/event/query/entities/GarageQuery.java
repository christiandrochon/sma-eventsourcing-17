package fr.cdrochon.smamonolithe.event.query.entities;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * En lien avec les entités Garage dans l'autre package
 *
 * Cette partie query est un microservice à part entiere, sur la base de lecture
 * Je recupere ces données sous forme d'events
 */
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GarageQuery {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String idQuery;
    private String nomGarage;
    private String mailResponsable;
//    private GarageStatus garageStatus;
//    private Instant dateQuery;


}

