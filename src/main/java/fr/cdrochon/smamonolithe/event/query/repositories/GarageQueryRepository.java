package fr.cdrochon.smamonolithe.event.query.repositories;

import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageQueryRepository extends JpaRepository<GarageQuery, String> {
}
