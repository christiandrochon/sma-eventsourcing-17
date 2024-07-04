package fr.cdrochon.smamonolithe.document.query.controllers;


import fr.cdrochon.smamonolithe.document.query.dtos.DocumentResponseDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeMapper;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class DocumentRestController {
    
    private final QueryGateway queryGateway;
    private final DocumentRepository documentRepository;
    
    public DocumentRestController(QueryGateway queryGateway, DocumentRepository documentRepository) {
        this.queryGateway = queryGateway;
        this.documentRepository = documentRepository;
    }
    
    /**
     * Retourne les informations d'un document accompagné des informations du vehicule qu'il concerne
     *
     * @param id id de document
     * @return document
     */
    @GetMapping("/documents/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public DocumentResponseDTO getDocumentById(@PathVariable String id) {
        GetDocumentDTO documentDTO = new GetDocumentDTO();
        documentDTO.setId(id);
        return queryGateway.query(documentDTO, DocumentResponseDTO.class).join();
    }

    /**
     * Retourne tous les documents concernant un vehicule
     *
     * @return liste de documents
     */
    @GetMapping("/documents")
//    @PreAuthorize("hasAuthority('USER')")
    public List<DocumentResponseDTO> getDocuments() {
        List<Document> documents = documentRepository.findAll();
        return documents.stream().map(DocumentMapper::convertDocumentToDocumentDTO).collect(Collectors.toList());
        
    }
    
    /**
     * Renvoi un flux de documentResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du document
     * @return Flux de documentResponseDTO
     */
    @GetMapping(value = "/document/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DocumentResponseDTO> watch(@PathVariable String id) {
        
        try(SubscriptionQueryResult<DocumentResponseDTO, DocumentResponseDTO> result = queryGateway.subscriptionQuery(
                new GetDocumentDTO(id),
                ResponseTypes.instanceOf(DocumentResponseDTO.class),
                ResponseTypes.instanceOf(DocumentResponseDTO.class)
                                                                                                                     )) {
            return result.initialResult().concatWith(result.updates());
        }
    }

}

