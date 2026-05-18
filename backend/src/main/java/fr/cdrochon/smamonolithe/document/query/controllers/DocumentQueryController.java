package fr.cdrochon.smamonolithe.document.query.controllers;

import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentQueryMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class DocumentQueryController {

    private final DocumentRepository documentRepository;

    public DocumentQueryController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // -------------------------------------------------------------------------
    // GET /queries/documents/{id}  – lecture unitaire avec IDOR fix
    // -------------------------------------------------------------------------

    /**
     * Retourne un document par son id.
     * RBAC : ADMIN voit tout. USER ne peut consulter que ses propres documents.
     */
    @GetMapping(path = "/documents/{id}")
    public Mono<DocumentQueryDTO> getDocumentByIdAsync(@PathVariable String id,
                                                       Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email    = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_DOCUMENT_READ_REQUEST documentId={} isAdmin={}", id, isAdmin);

        return Mono.fromCallable(() -> {
            Document doc = documentRepository.findById(id).orElse(null);
            if (doc == null) {
                BusinessLoggers.business().info("BIZ_DOCUMENT_READ_NOT_FOUND documentId={}", id);
                return null;
            }
            // IDOR fix : un USER ne peut lire que ses propres documents
            if (!isAdmin) {
                if (email == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Acces refuse: utilisateur sans email JWT");
                }
                String ownerEmail = doc.getClient() != null
                        ? doc.getClient().getMailClient()
                        : null;
                if (!email.equals(ownerEmail)) {
                    BusinessLoggers.business().warn(
                            "BIZ_DOCUMENT_READ_FORBIDDEN documentId={} email={}", id, email);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Accès refusé : ce document ne vous appartient pas");
                }
            }
            BusinessLoggers.business().info("BIZ_DOCUMENT_READ_SUCCESS documentId={}", id);
            return DocumentQueryMapper.convertDocumentToDocumentDTO(doc);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // -------------------------------------------------------------------------
    // GET /queries/documents  – liste filtrée par rôle
    // -------------------------------------------------------------------------

    /**
     * Retourne les documents accessibles à l'utilisateur connecté.
     * ADMIN → tous les documents.
     * USER  → uniquement les documents où client.mailClient == email JWT.
     */
    @GetMapping(path = "/documents")
    public Flux<DocumentQueryDTO> getDocumentsAsync(Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email    = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_DOCUMENT_LIST_REQUEST isAdmin={} email={}", isAdmin, email);

        return Mono.fromCallable(() -> {
            List<DocumentQueryDTO> docs;
            if (isAdmin) {
                docs = documentRepository.findAll()
                        .stream()
                        .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                        .collect(Collectors.toList());
            } else {
                if (email == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Acces refuse: utilisateur sans email JWT");
                }
                docs = documentRepository.findByClientMailClient(email)
                        .stream()
                        .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                        .collect(Collectors.toList());
            }
            BusinessLoggers.business().info("BIZ_DOCUMENT_LIST_SUCCESS count={}", docs.size());
            return docs;
        }).subscribeOn(Schedulers.boundedElastic())
          .flatMapMany(Flux::fromIterable);
    }

    // Compatibilite tests unitaires existants (hors couche web/security).
    Mono<DocumentQueryDTO> getDocumentByIdAsync(String id) {
        return Mono.fromCallable(() -> documentRepository.findById(id)
                .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                .orElse(null));
    }

    // Compatibilite ancien nom de methode de liste.
    Flux<DocumentQueryDTO> getDossiersAsync() {
        return Flux.fromIterable(documentRepository.findAll().stream()
                .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                .collect(Collectors.toList()));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private boolean hasRole(Jwt jwt, String role) {
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                Object roles = realmAccess.get("roles");
                if (roles instanceof java.util.List<?> list) {
                    return list.contains(role) || list.contains("ROLE_" + role);
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
