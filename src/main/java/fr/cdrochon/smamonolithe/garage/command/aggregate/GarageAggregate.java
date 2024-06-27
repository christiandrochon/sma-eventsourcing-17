package fr.cdrochon.smamonolithe.garage.command.aggregate;

import fr.cdrochon.smamonolithe.garage.command.commands.GarageCreateCommand;
import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.events.GarageCreatedEvent;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * UN agregat correspond à l'etat stable courant de l'appli
 * <p>
 * Il permet de traiter une commande
 */
@Aggregate
public class GarageAggregate {
    
    @AggregateIdentifier
    private String id;
    private String nomGarage;
    private String mailResponsable;
    private AdresseGarage adresseGarage;
    private GarageStatus status;
    private Instant date;
    
    /**
     * OBLIGATOIRE , requis par AXON
     */
    public GarageAggregate() {
        // required by Axon
    }
    
    /**
     * FONCTION DE DECISION = regle metier
     *
     * Publiation d'un event via AggregateLifeCycle.apply(). Normalement, cet event devrait etre enregistré dans l'event store
     * <p>
     * Prise en charge d'une commande = FONCTION DE DECISION pour la creation d'un client
     * <p>
     * Instancie un nouvel agregat à chaque requete recue
     * ici => fonction de decision = verifie regle metier
     * <p>
     * (explication de youssfi pour @CommandHandler : Subscribe à commandBus -> dès qu'il y a une command, j'instancie un nouvel agregat grace au
     *  constructeur par defaut)
     *
     *
     * @param createGarageCommand
     */
    @CommandHandler
    public GarageAggregate(GarageCreateCommand createGarageCommand) {
        
        //ici => fonction de decision = verifie regle metier
        if(createGarageCommand.getNomGarage() == null) {
            throw new CreatedGarageException("Le nom du garage doit etre renseigné ! ");
        }
        //publication de l'event
        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new GarageCreatedEvent(createGarageCommand.getId(),
                                                        createGarageCommand.getNomGarage(),
                                                        createGarageCommand.getMailResponsable(),
                                                        createGarageCommand.getAdresseGarage(),
                                                        GarageStatus.CREATED
        ));
    }
    
    /**
     * FONCTION D'EVOLUTION = Muter l'etat de l'agregat
     *
     * Pour chaque event de type ClientCreatedEvent qui arrive dans l'eventstore, on va muter l'etat de l'application
     *
     * @param event
     */
    @EventSourcingHandler
    public void on(GarageCreatedEvent event) {
     
        System.out.println("**********************");
        System.out.println("Agregat Enventsourcinghandler ");
        this.id = event.getId();
        this.nomGarage = event.getNomGarage();
        this.mailResponsable = event.getMailResponsable();
        this.status = event.getClientStatus();
        //        this.date = event.getDateQuery();
        //AggregateLifecycle.apply(new GarageQueryCreatedEvent(id, nomGarage, mailResponsable, status, date));
    }
    
    
}
