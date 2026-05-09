package fr.cdrochon.smamonolithe.garage.query.repositories;

import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageRepository extends JpaRepository<Garage, String> {
}
