package fr.cdrochon.smamonolithe.document.query.repositories;

import fr.cdrochon.smamonolithe.document.query.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {

    /** Retourne tous les documents appartenant au client identifié par son email. */
    List<Document> findByClientMailClient(String mailClient);
}
