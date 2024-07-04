package fr.cdrochon.smamonolithe.document.command.aggregate;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatus;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;
@Aggregate
public class DocumentAggregate {
    @AggregateIdentifier
    private String idDocument;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatus documentStatus;
    
    public DocumentAggregate() {
        //requis par Axon
    }
    
    /**
     * FONCTION DE DECISION (regle metier)
     * <p>
     * Publiation d'un event via AggregateLifeCycle.apply() et l'enregistre dans l'event store
     *
     * @param createDocumentCommand command
     */
    @CommandHandler
    public DocumentAggregate(DocumentCreateCommand createDocumentCommand) {

        //ici => fonction de decision = verifie regle metier
        if(createDocumentCommand.getNomDocument() == null) {
            throw new CreatedGarageException("Le document doit exister ! ");
        }

        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new DocumentCreatedEvent(createDocumentCommand.getId(),
                                                          createDocumentCommand.getNomDocument(),
                                                          createDocumentCommand.getTitreDocument(),
                                                            createDocumentCommand.getEmetteurDuDocument(),
                                                            createDocumentCommand.getTypeDocument(),
                                                            createDocumentCommand.getDateCreationDocument(),
                                                            createDocumentCommand.getDateModificationDocument(),
                                                            DocumentStatus.CREATED
        ));
        System.out.println("**************************");
    }

    /**
     * FONCTION D'EVOLUTION (muter l'etat de l'agregat)
     * <p>
     * Pour chaque event de type DocumentCreatedEvent qui arrive dans l'eventstore, on va muter l'etat de l'application
     *
     * @param event
     */
    @EventSourcingHandler
    public void on(DocumentCreatedEvent event) {

        System.out.println("**********************");
        System.out.println("Agregat Enventsourcinghandler ");
        this.idDocument = event.getId();
        this.nomDocument = event.getNomDocument();
        this.titreDocument = event.getTitreDocument();
        this.emetteurDuDocument = event.getEmetteurDuDocument();
        this.typeDocument = event.getTypeDocument();
        this.dateCreationDocument = event.getDateCreationDocument();
        this.dateModificationDocument = event.getDateModificationDocument();
        this.documentStatus = event.getDocumentStatus();
        //AggregateLifecycle.apply(new GarageQueryCreatedEvent(id, nomGarage, mailResponsable, status, date));
    }
}
