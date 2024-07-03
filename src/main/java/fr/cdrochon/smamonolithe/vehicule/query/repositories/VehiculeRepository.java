package fr.cdrochon.smamonolithe.vehicule.query.repositories;

import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, String> {
}
