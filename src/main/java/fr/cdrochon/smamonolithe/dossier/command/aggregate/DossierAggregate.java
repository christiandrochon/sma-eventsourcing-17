package fr.cdrochon.smamonolithe.dossier.command.aggregate;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

@Aggregate @Getter @Setter
public class DossierAggregate {
    
    @AggregateIdentifier
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private Client client;
    private Vehicule vehicule;
    private DossierStatus dossierStatus;
    
    public DossierAggregate() {
        //requis par Axon
    }
    
    /**
     * <str>Fonction de décision</str> = vérifie règle métier
     * <p>
     * Instancie un nouvel agrégat à chaque requête reçue dans le bus de commande via le @CommandHandler qui subscribe au commandBus.
     * <p>
     * AggregateLifecycle.apply() = publication de l'event
     *
     * @param dossierCreateCommand Commande de création d'un dossier
     */
    @CommandHandler
    public DossierAggregate(DossierCreateCommand dossierCreateCommand) {
        
        //ici => fonction de decision = verifie regle metier
        if(dossierCreateCommand.getVehicule() == null || dossierCreateCommand.getClient() == null){
            throw new CreatedGarageException("Le dossier doit contenir un client et un vehicule ! ");
        }

        //publication de l'event
        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new DossierCreatedEvent(dossierCreateCommand.getId(),
                                                        dossierCreateCommand.getNomDossier(),
                                                        dossierCreateCommand.getDateCreationDossier(),
                                                        dossierCreateCommand.getDateModificationDossier(),
                                                        dossierCreateCommand.getClient(),
                                                        dossierCreateCommand.getVehicule(),
                                                        dossierCreateCommand.getDossierStatus(),
                                                        dossierCreateCommand.getClient().getId(),
                                                        dossierCreateCommand.getVehicule().getIdVehicule()
        ));
    }
    
    /**
     * <str>Fonction de projection</str> = met à jour l'état de l'agrégat
     * <p>
     * Met à jour l'état de l'agrégat à chaque événement reçu dans le bus d'événement via le @EventSourcingHandler qui subscribe à l'eventBus.
     *
     * @param event Evènement de création d'un dossier
     */
    @EventSourcingHandler
    public void on(DossierCreatedEvent event) {
        
        System.out.println("**********************");
        System.out.println("Agregat Enventsourcinghandler ");
        this.id = event.getId();
        this.nomDossier = event.getNomDossier();
        this.dateCreationDossier = event.getDateCreationDossier();
        this.dateModificationDossier = event.getDateModificationDossier();
        this.client = event.getClient();
        this.vehicule = event.getVehicule();
        this.dossierStatus = event.getDossierStatus();
        
        this.client.setId(event.getClient().getId());
        this.vehicule.setIdVehicule(event.getVehicule().getIdVehicule());
    }
}
