package fr.cdrochon.smamonolithe.event.query.entities;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idQuery;
    private Instant dateQuery;
    private String nomGarage;
    private String mailResponsable;
    private GarageStatus garageStatus;


}
//public record GarageQuery(Long id, Instant dateQuery, String nomGarage, String mailResp, GarageStatus garageStatus) {
//}
