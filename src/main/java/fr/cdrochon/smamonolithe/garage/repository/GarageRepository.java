package fr.cdrochon.smamonolithe.garage.repository;


import fr.cdrochon.smamonolithe.garage.entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
}
