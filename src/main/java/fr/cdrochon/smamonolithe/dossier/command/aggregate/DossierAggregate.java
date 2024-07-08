package fr.cdrochon.smamonolithe.dossier.command.aggregate;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

@Aggregate
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
     * FONCTION DE DECISION = regle metier
     * <p>
     * Publiation d'un event via AggregateLifeCycle.apply(). Normalement, cet event devrait etre enregistré dans l'event store
     * <p>
     * Prise en charge d'une commande = FONCTION DE DECISION pour la creation d'un dossier
     * <p>
     * Instancie un nouvel agregat à chaque requete recue
     * ici => fonction de decision = verifie regle metier
     * <p>
     * @CommandHandler : Subscribe à commandBus -> dès qu'il y a une command, j'instancie un nouvel agregat grace au constructeur par defaut
     *
     *
     * @param dossierCreateCommand Commande de creation d'un dossier
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
                                                        DossierStatus.OUVERT
        ));
    }
    
    /**
     * FONCTION D'EVOLUTION = Muter l'etat de l'agregat
     * <p>
     * Pour chaque event de type DossierCreatedEvent qui arrive dans l'eventstore, on va muter l'etat de l'application
     *
     * @param event Event de creation d'un client
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
        //AggregateLifecycle.apply(new GarageQueryCreatedEvent(id, nomGarage, mailResponsable, status, date));
    }
}
