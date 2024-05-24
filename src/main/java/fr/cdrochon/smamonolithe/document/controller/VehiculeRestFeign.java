package fr.cdrochon.smamonolithe.document.controller;

import fr.cdrochon.smamonolithe.document.model.MarqueVehicule;
import fr.cdrochon.smamonolithe.document.model.TypeVehicule;
import fr.cdrochon.smamonolithe.document.model.Vehicule;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tolerance aux pannes
 */
@FeignClient(name = "VEHICULE-SERVICE",url = "http://localhost:8092")
//@FeignClient(name = "VEHICULE-SERVICE", value = "getDefaultAllClients")
//@FeignClient(name = "VEHICULE-SERVICE")
public interface VehiculeRestFeign {

    @GetMapping("/vehicule/{id}")
//    @CircuitBreaker(label =  "vehiculeService", exceptionExpression = "getDefaultVehicule")
    @CircuitBreaker(name = "CircuitBreakerVehiculeService", fallbackMethod = "getDefaultVehicule")
    Vehicule findVehiculeById(@PathVariable Long id);

    @GetMapping("/vehicules")
//    @CircuitBreaker(label = "vehiculeService", exceptionExpression = "getDefaultAllVehicules")
    @CircuitBreaker(name = "CircuitBreakerVehiculeService", fallbackMethod = "getDefaultAllVehicules")
    List<Vehicule> findEveryVehicules();

    /**
     * Comportement par defaut lorsque le microservice vehicule ne repond pas.
     *
     * @param exception type exception
     * @return objet vehicule vide
     */
    default Vehicule getDefaultVehicule(Exception exception) {

        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setImmatriculationVehicule("Non disponible");
        vehicule.setDateMiseEnCirculationVehicule(LocalDate.of(2000, 1, 1));
        vehicule.setTypeVehicule(TypeVehicule.NON_DISPONIBLE);
        vehicule.setMarqueVehicule(MarqueVehicule.NON_DISPONIBLE);
        vehicule.setClimatisationVehicule(false);
        System.err.println("Exception default getDefaultVehicule : " + exception.getMessage());
        return vehicule;
    }

    /**
     * Retourne une liste vide de vehicules par defaut
     *
     * @param exception type exception
     * @return liste vide de vehicules
     */
    default List<Vehicule> getDefaultAllVehicules(Exception exception) {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules.forEach(v->{
            //v.setId(UUID.randomUUID().node());
            v.setImmatriculationVehicule("Immatriculation non disponible");
            v.setDateMiseEnCirculationVehicule(LocalDate.of(2020,01,01));
            v.setTypeVehicule(TypeVehicule.NON_DISPONIBLE);
            v.setMarqueVehicule(MarqueVehicule.NON_DISPONIBLE);
            v.setClimatisationVehicule(false);
        });
        System.err.println("Exception default getDefaultAllVehicules : " + exception.getMessage());
        return vehicules;
//        return List.of();
    }
}
