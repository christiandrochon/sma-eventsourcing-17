package fr.cdrochon.thymeleaffrontend.repository;

import fr.cdrochon.thymeleaffrontend.entity.garage.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageRepository extends JpaRepository<Garage, String> {
}
