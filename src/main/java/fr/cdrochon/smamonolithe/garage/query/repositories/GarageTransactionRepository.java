package fr.cdrochon.smamonolithe.garage.query.repositories;

import fr.cdrochon.smamonolithe.garage.query.entities.GarageTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageTransactionRepository extends JpaRepository<GarageTransaction, Long> {
}
