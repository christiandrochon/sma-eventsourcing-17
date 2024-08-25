package fr.cdrochon.smamonolithe.document.query.controllers;


import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentQueryMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import lombok.val;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class DocumentQueryController {
    
    private final QueryGateway queryGateway;
    private final DocumentRepository documentRepository;
    
    public DocumentQueryController(QueryGateway queryGateway, DocumentRepository documentRepository) {
        this.queryGateway = queryGateway;
        this.documentRepository = documentRepository;
    }
    
    /**
     * Retourne les informations d'un document accompagné des informations du vehicule qu'il concerne
     *
     * @param id id de document
     * @return document
     */
    @GetMapping(path = "/documents/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<DocumentQueryDTO> getDocumentByIdAsync(@PathVariable String id) {
        CompletableFuture<DocumentQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    DocumentQueryDTO doc = documentRepository.findById(id)
                                                .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                                                .orElse(null);
                    return doc;
                });
        Mono<DocumentQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    //    @GetMapping("/documents/{id}")
    ////    @PreAuthorize("hasAuthority('USER')")
    //    public DocumentQueryDTO getDocumentById(@PathVariable String id) {
    //        GetDocumentDTO documentDTO = new GetDocumentDTO();
    //        documentDTO.setId(id);
    //        return queryGateway.query(documentDTO, DocumentQueryDTO.class).join();
    //    }
    
    /**
     * Retourne tous les documents concernant un vehicule
     *
     * @return liste de documents
     */
    @GetMapping(path = "/documents")
    //    @PreAuthorize("hasAuthority('USER')")
    public Flux<DocumentQueryDTO> getDossiersAsync() {
        CompletableFuture<List<DocumentQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            List<DocumentQueryDTO> clients =
                    documentRepository.findAll()
                                      .stream()
                                      .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                                      .collect(Collectors.toList());
            return clients;
        });
        Flux<DocumentQueryDTO> flux = Flux.fromStream(future.join().stream());
        return flux;
    }
    //    @GetMapping("/documents")
    ////    @PreAuthorize("hasAuthority('USER')")
    //    public List<DocumentQueryDTO> getDocuments() {
    //        List<Document> documents = documentRepository.findAll();
    //        return documents.stream().map(DocumentQueryMapper::convertDocumentToDocumentDTO).collect(Collectors.toList());
    //
    //    }
    
    /**
     * Renvoi un flux de documentResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du document
     * @return Flux de documentResponseDTO
     */
    //    @GetMapping(value = "/document/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    //    public Flux<DocumentResponseDTO> watch(@PathVariable String id) {
    //
    //        try(SubscriptionQueryResult<DocumentResponseDTO, DocumentResponseDTO> result = queryGateway.subscriptionQuery(
    //                new GetDocumentDTO(id),
    //                ResponseTypes.instanceOf(DocumentResponseDTO.class),
    //                ResponseTypes.instanceOf(DocumentResponseDTO.class)
    //                                                                                                                     )) {
    //            return result.initialResult().concatWith(result.updates());
    //        }
    //    }
    
}

