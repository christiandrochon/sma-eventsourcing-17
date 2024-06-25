package fr.cdrochon.smamonolithe.event.query.repositories;

import fr.cdrochon.smamonolithe.event.query.entities.GarageQueryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageQueryTransactionRepository extends JpaRepository<GarageQueryTransaction, Long> {
}
