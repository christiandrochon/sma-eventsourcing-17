package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

@RestController
@RequestMapping("/commands")
public class DocumentCommandController {
    
    private final EventStore eventStore;
    
    private final DocumentCommandService documentCommandService;
    private final ClientRepository clientRepository;

    @Autowired
    public DocumentCommandController(DocumentCommandService documentCommandService,
                                     EventStore eventStore,
                                     ClientRepository clientRepository) {
        this.eventStore = eventStore;
        this.documentCommandService = documentCommandService;
        this.clientRepository = clientRepository;
    }

    public DocumentCommandController(DocumentCommandService documentCommandService,
                                     EventStore eventStore) {
        this(documentCommandService, eventStore, null);
    }

    /**
     * Création d'un document de manière asynchrone
     *
     * @param documentCommandDTO DTO de création d'un document
     * @return ResponseEntity<DocumentCommandDTO> DTO de création d'un document
     */
    @PostMapping(value = "/createDocument")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Mono<ResponseEntity<DocumentCommandDTO>> createClientAsync(@RequestBody DocumentCommandDTO documentCommandDTO,
                                                                      Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        String email = jwt != null ? jwt.getClaimAsString("email") : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");

        // USER : impose le client propriétaire à partir de l'email JWT.
        // ADMIN : peut fournir clientId, sinon fallback sur son email si un client existe.
        if (email != null) {
            Client owner = clientRepository.findByMailClient(email).orElse(null);
            if (owner != null && (!isAdmin || documentCommandDTO.getClientId() == null)) {
                documentCommandDTO.setClientId(owner.getId());
            }
        }

        if (documentCommandDTO.getClientId() == null) {
            BusinessLoggers.business().warn("BIZ_DOCUMENT_CREATE_FORBIDDEN_NO_CLIENT email={} isAdmin={}", email, isAdmin);
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }

        return Mono.fromFuture(documentCommandService.createDocument(documentCommandDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(document -> {
                       BusinessLoggers.business().info("BIZ_DOCUMENT_CREATE_OK documentId={}", document.getId());
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(document));
                   })
                   .onErrorResume(ex -> {
                       BusinessLoggers.business().error("BIZ_DOCUMENT_CREATE_FAILED message={}", ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }

    // Compatibilite tests unitaires existants (sans authentification explicite).
    public Mono<ResponseEntity<DocumentCommandDTO>> createClientAsync(DocumentCommandDTO documentCommandDTO) {
        return Mono.fromFuture(documentCommandService.createDocument(documentCommandDTO))
                .subscribeOn(Schedulers.boundedElastic())
                .map(document -> ResponseEntity.status(HttpStatus.CREATED).body(document))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    private boolean hasRole(Jwt jwt, String role) {
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                Object roles = realmAccess.get("roles");
                if (roles instanceof java.util.List<?> list) {
                    return list.contains(role) || list.contains("ROLE_" + role);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés) Le format renvoyé
     * est du json dans swagger
     *
     * @param id id de l'agregat
     * @return Stream
     */
    @GetMapping(path = "/eventStoreDocument/{id}")
    public Stream readDocumentsInEventStore(@PathVariable String id) {
        return eventStore.readEvents(id).asStream();
    }
    
    
    /**
     * Pour recuperer les messages d'erreur lorsqu'une requete s'est mal passée
     *
     * @param exception exception
     * @return message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        BusinessLoggers.business().error("BIZ_DOCUMENT_CREATE_FAILED message={} exceptionType={}",
                                        exception.getMessage(),
                                        exception.getClass().getSimpleName());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
