package fr.cdrochon.smamonolithe.document.controller;


import fr.cdrochon.smamonolithe.document.repository.DocumentRepository;
import fr.cdrochon.smamonolithe.document.entity.Document;
import fr.cdrochon.smamonolithe.vehicule.entity.MarqueVehicule;
import fr.cdrochon.smamonolithe.vehicule.entity.TypeVehicule;
import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.repository.VehiculeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class DocumentRestController {
    
    private final DocumentRepository documentRepository;
    private final VehiculeRepository vehiculeRepository;
    
    public DocumentRestController(DocumentRepository documentRepository,
                                  VehiculeRepository vehiculeRepository) {
        this.documentRepository = documentRepository;
        this.vehiculeRepository = vehiculeRepository;
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
        if(document.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(document.getVehiculeId()).get();
            document.setVehicule(vehicule);
        }
        else {
            document.setVehicule(getDefaultVehicule(new Exception("Vehicule inexistant")));
        }
        
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
            if(doc.getVehiculeId() != null) {
                doc.setVehicule(vehiculeRepository.findById(doc.getVehiculeId()).get());
            }
            else {
                doc.setVehicule(getDefaultVehicule(new Exception("Vehicule inexistant")));
            }
        });
        return documents;
    }
    
    /**
     * Comportement par defaut lorsque le microservice vehicule ne repond pas.
     *
     * @param exception type exception
     * @return objet vehicule vide
     */
    private Vehicule getDefaultVehicule(Exception exception) {
        
        Vehicule vehicule = new Vehicule();
        vehicule.setId(new Random().nextLong());
        vehicule.setImmatriculationVehicule("Non disponible");
        vehicule.setDateMiseEnCirculationVehicule(LocalDate.of(2000, 1, 1));
        vehicule.setTypeVehicule(TypeVehicule.NON_DISPONIBLE);
        vehicule.setMarqueVehicule(MarqueVehicule.NON_DISPONIBLE);
        vehicule.setClimatisationVehicule(false);
        System.err.println("Exception default getDefaultVehicule : " + exception.getMessage());
        return vehicule;
    }
    
}

