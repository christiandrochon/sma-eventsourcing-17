package fr.cdrochon.smamonolithe.event.command.aggregate;

import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import fr.cdrochon.smamonolithe.event.commonapi.events.GarageQueryCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * UN agregat correspond à l'etat stable courant de l'appli
 * <p>
 * Il permet de traiter une commande
 */
@Profile("command")
@Aggregate(cache = "garageQueryCache")
public class GarageQueryAggregate {
    
    @AggregateIdentifier
    private String id;
    private String nomGarage;
    private String mailResponsable;
    private GarageStatus status;
    private Instant date;
    
    /**
     * oBLIGATOIRE , requis par AXON
     */
    public GarageQueryAggregate() {
        // required by Axon
    }
    
    /**
     * Publiation d'un event via AggregateLifeCycle.apply(). Noirlalement, cet event devrait etre enregistré dans l'event store
     * <p>
     * Prise en charge d'un commande = FONCTION DE DECISION pour la creation d'un client
     * <p>
     * Instancie un nouvel agregat à chaque requete recue
     * ici => fonction de decision = verifie regle metier
     *
     * @param createGarageCommand
     */
    @CommandHandler
    public GarageQueryAggregate(GarageQueryCreateCommand createGarageCommand) {
        //ici => fonction de decision = verifie regle metier
        //        if(createGarageCommand.nomClient() == null) {
        //            throw new CreatedClientException("Client non complet ! ");
        //        }
        //publication de l'event
        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        
        AggregateLifecycle.apply(new GarageQueryCreatedEvent(createGarageCommand.getId(), createGarageCommand.getNomClient(),
                                                             createGarageCommand.getMailResponsable(),
                                                             GarageStatus.CREATED, createGarageCommand.getDateQuery()));
        //        apply(new GarageQueryCreatedEvent(createGarageCommand.id(), createGarageCommand.nomClient(),
        //                                                             createGarageCommand.mailResp(),
        //                                                             GarageStatus.CREATED));
    }
    
    /**
     * FONCTION D'EVOLUTION = pour chaque event de type ClientCreatedEvent, on va muter l'etat de l'application
     *
     * @param event
     */
    @EventSourcingHandler
    public void on(GarageQueryCreatedEvent event) {
        //        id = event.id();
        //        nomGarage = event.nomClient();
        //        mailResponsable = event.mailResp();
        //        status = event.garageStatus();
        
        
        this.id = event.getId();
        this.nomGarage = event.getNomGarage();
        this.mailResponsable = event.getMailResponsable();
        this.status = event.getClientStatus();
        this.date = event.getDateQuery();
    }
    
    
}
