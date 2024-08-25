package fr.cdrochon.smamonolithe.document.command.aggregate;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

@Aggregate
@Getter
@Setter
@Slf4j
public class DocumentAggregate {
    
    @AggregateIdentifier
    private String idDocument;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatusDTO documentStatus;
    
    public DocumentAggregate() {
        //requis par Axon
    }
    
    /**
     * <str>Fonction de décision</str> = vérifie règle métier
     * <p>
     * Instancie un nouvel agrégat à chaque requête reçue dans le bus de commande via le @CommandHandler qui subscribe au commandBus.
     * <p>
     * AggregateLifecycle.apply() = publication de l'event
     *
     * @param createDocumentCommand Commande de création d'un document
     */
    @CommandHandler
    public DocumentAggregate(DocumentCreateCommand createDocumentCommand) {
        
        //ici => fonction de decision = verifie regle metier
        if(createDocumentCommand.getNomDocument() == null) {
            throw new CreatedGarageException("Le document doit exister ! ");
        }
        
        
        log.info("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new DocumentCreatedEvent(createDocumentCommand.getId(),
                                                          createDocumentCommand.getNomDocument(),
                                                          createDocumentCommand.getTitreDocument(),
                                                          createDocumentCommand.getEmetteurDuDocument(),
                                                          createDocumentCommand.getTypeDocument(),
                                                          createDocumentCommand.getDateCreationDocument(),
                                                          createDocumentCommand.getDateModificationDocument(),
                                                          createDocumentCommand.getDocumentStatus()
        ));
    }
    
    /**
     * <str>Fonction de projection</str> = met à jour l'état de l'agrégat
     *
     * @param event l'event DocumentCreatedEvent
     */
    @EventSourcingHandler
    public void on(DocumentCreatedEvent event) {
        
        log.info("Agregat Enventsourcinghandler ");
        this.idDocument = event.getId();
        this.nomDocument = event.getNomDocument();
        this.titreDocument = event.getTitreDocument();
        this.emetteurDuDocument = event.getEmetteurDuDocument();
        this.typeDocument = event.getTypeDocument();
        this.dateCreationDocument = event.getDateCreationDocument();
        this.dateModificationDocument = event.getDateModificationDocument();
        this.documentStatus = event.getDocumentStatus();
    }
}
