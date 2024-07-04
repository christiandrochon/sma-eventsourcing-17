package fr.cdrochon.smamonolithe.document.query.repositories;

import fr.cdrochon.smamonolithe.document.query.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
}
