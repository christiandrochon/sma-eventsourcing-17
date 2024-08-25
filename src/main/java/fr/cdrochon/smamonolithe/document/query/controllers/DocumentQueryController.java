package fr.cdrochon.smamonolithe.document.query.controllers;


import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentQueryMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
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
    
    
    private final DocumentRepository documentRepository;
    
    public DocumentQueryController(DocumentRepository documentRepository) {
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
}

