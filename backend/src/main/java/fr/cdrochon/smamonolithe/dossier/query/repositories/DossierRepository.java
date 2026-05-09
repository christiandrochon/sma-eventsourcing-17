package fr.cdrochon.smamonolithe.dossier.query.repositories;

import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, String> {
}
