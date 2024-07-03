package fr.cdrochon.smamonolithe.vehicule.query.repositories;

import fr.cdrochon.smamonolithe.vehicule.query.entities.VehiculeResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculeRepository extends JpaRepository<VehiculeResponseDTO, String> {
}
