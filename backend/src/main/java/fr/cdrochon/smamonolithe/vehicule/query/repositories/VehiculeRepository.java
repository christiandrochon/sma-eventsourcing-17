package fr.cdrochon.smamonolithe.vehicule.query.repositories;

import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, String> {
    
    Vehicule findByImmatriculationVehicule(String immatriculation);
    Boolean existsByImmatriculationVehicule(String immatriculation);
    List<Vehicule> findByClientMailClient(String mailClient);
}
