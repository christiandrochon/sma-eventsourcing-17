package fr.cdrochon.smamonolithe.document.command.services;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DocumentCommandService {
    
    private static final long CREATE_TIMEOUT_SECONDS = 20L;

    private final CommandGateway commandGateway;
    // Une future par documentId pour supporter les creations concurrentes.
    private final ConcurrentMap<String, CompletableFuture<DocumentCommandDTO>> pendingCreations = new ConcurrentHashMap<>();

    public DocumentCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de document
     *
     * @param documentRestDTO contenant les informations du document a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<DocumentCommandDTO> createDocument(DocumentCommandDTO documentRestDTO) {
        String documentId = UUID.randomUUID().toString();
        CompletableFuture<DocumentCommandDTO> futureDTO = new CompletableFuture<>();
        pendingCreations.put(documentId, futureDTO);

        BusinessLoggers.business().info("BIZ_DOCUMENT_CREATE_REQUEST documentId={} nomDocument={} type={} status={}",
                                        documentId,
                                        documentRestDTO.getNomDocument(),
                                        documentRestDTO.getTypeDocument(),
                                        documentRestDTO.getDocumentStatus());

        DocumentCreateCommand command = new DocumentCreateCommand(documentId,
                                                                  documentRestDTO.getNomDocument(),
                                                                  documentRestDTO.getTitreDocument(),
                                                                  documentRestDTO.getEmetteurDuDocument(),
                                                                  documentRestDTO.getTypeDocument(),
                                                                  documentRestDTO.getDateCreationDocument(),
                                                                  documentRestDTO.getDateModificationDocument(),
                                                                  documentRestDTO.getDocumentStatus());

        DocumentCommandDTO ackDTO = new DocumentCommandDTO(documentId,
                                                           documentRestDTO.getNomDocument(),
                                                           documentRestDTO.getTitreDocument(),
                                                           documentRestDTO.getEmetteurDuDocument(),
                                                           documentRestDTO.getTypeDocument(),
                                                           documentRestDTO.getDateCreationDocument(),
                                                           documentRestDTO.getDateModificationDocument(),
                                                           documentRestDTO.getDocumentStatus());

        commandGateway.send(command).whenComplete((ignored, error) -> {
            CompletableFuture<DocumentCommandDTO> pending = pendingCreations.get(documentId);
            if(pending == null) {
                return;
            }

            if(error != null) {
                pendingCreations.remove(documentId);
                BusinessLoggers.business().error("BIZ_DOCUMENT_CREATE_FAILED documentId={} message={}",
                                                 documentId,
                                                 error.getMessage());
                pending.completeExceptionally(error);
                return;
            }

            // Command accepted by Axon: complete the async REST flow even if projection handlers are delayed.
            pendingCreations.remove(documentId);
            BusinessLoggers.business().info("BIZ_DOCUMENT_CREATE_CONFIRMED documentId={} nomDocument={} status={}",
                                            ackDTO.getId(),
                                            ackDTO.getNomDocument(),
                                            ackDTO.getDocumentStatus());
            pending.complete(ackDTO);
        });

        return futureDTO.orTimeout(CREATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .whenComplete((ok, err) -> pendingCreations.remove(documentId));
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeDocumentCreation(DocumentCommandDTO dto) {
        CompletableFuture<DocumentCommandDTO> pending = pendingCreations.remove(dto.getId());
        if(pending != null) {
            BusinessLoggers.business().info("BIZ_DOCUMENT_CREATE_CONFIRMED documentId={} nomDocument={} status={}",
                                            dto.getId(),
                                            dto.getNomDocument(),
                                            dto.getDocumentStatus());
            pending.complete(dto);
        } else {
            log.warn("TECH_DOCUMENT_CREATE_FUTURE_MISSING documentId={} (event recu sans future en attente)", dto.getId());
        }
    }
    
}
