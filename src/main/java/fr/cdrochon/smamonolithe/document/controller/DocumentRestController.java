package fr.cdrochon.smamonolithe.document.controller;


import fr.cdrochon.smamonolithe.document.repository.DocumentRepository;
import fr.cdrochon.smamonolithe.document.entity.Document;
import fr.cdrochon.smamonolithe.document.model.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DocumentRestController {
    
    private final DocumentRepository documentRepository;
    private final VehiculeRestFeign vehiculeRestFeign;
    
    public DocumentRestController(DocumentRepository documentRepository,
                                  VehiculeRestFeign vehiculeRestFeign) {
        this.documentRepository = documentRepository;
        this.vehiculeRestFeign = vehiculeRestFeign;
    }
    
    /**
     * Retourne les informations d'un document accompagné des informations du vehicule qu'il concerne
     *
     * @param id id de document
     * @return document
     */
    @GetMapping("/document/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Document getDocumentById(@PathVariable Long id) {
        Document document = documentRepository.findById(id).get();
        
        Vehicule vehicule = vehiculeRestFeign.findVehiculeById(document.getId());
        document.setVehicule(vehicule);
        return document;
    }
    
    /**
     * Retourne tous les documents concernant un vehicule
     *
     * @return liste de documents
     */
    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('USER')")
    public List<Document> getDocuments() {
        List<Document> documents = documentRepository.findAll();
        
        documents.forEach(doc -> {
            doc.setVehicule(vehiculeRestFeign.findVehiculeById(doc.getVehiculeId()));
        });
        return documents;
    }
}
