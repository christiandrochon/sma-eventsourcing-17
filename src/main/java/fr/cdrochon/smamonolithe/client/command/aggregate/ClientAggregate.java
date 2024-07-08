package fr.cdrochon.smamonolithe.client.command.aggregate;

import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.command.enums.ClientStatus;
import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class ClientAggregate {
    
    @AggregateIdentifier
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClient adresseClient;
    private ClientStatus clientStatus;
    
    public ClientAggregate() {
        //requis par Axon);
    }
    
    /**
     * FONCTION DE DECISION = regle metier
     * <p>
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
     * @param createClientCommand Commande de creation d'un client
     */
    @CommandHandler
    public ClientAggregate(ClientCreateCommand createClientCommand) {
        
        //ici => fonction de decision = verifie regle metier
        if(createClientCommand.getNomClient() == null) {
            throw new CreatedGarageException("Le nom du client doit etre renseigné ! ");
        }
        //publication de l'event
        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new ClientCreatedEvent(createClientCommand.getId(),
                                                        createClientCommand.getNomClient(),
                                                        createClientCommand.getPrenomClient(),
                                                        createClientCommand.getMailClient(),
                                                        createClientCommand.getTelClient(),
                                                        createClientCommand.getAdresseClient(),
                                                        ClientStatus.ACTIF
        ));
    }
    
    /**
     * FONCTION D'EVOLUTION = Muter l'etat de l'agregat
     * <p>
     * Pour chaque event de type ClientCreatedEvent qui arrive dans l'eventstore, on va muter l'etat de l'application
     *
     * @param event Event de creation d'un client
     */
    @EventSourcingHandler
    public void on(ClientCreatedEvent event) {
        
        System.out.println("**********************");
        System.out.println("Agregat Enventsourcinghandler ");
        this.id = event.getId();
        this.nomClient = event.getNomClient();
        this.prenomClient = event.getPrenomClient();
        this.mailClient = event.getMailClient();
        this.telClient = event.getTelClient();
        this.adresseClient = event.getAdresseClient();
        this.clientStatus = event.getClientStatus();
        //AggregateLifecycle.apply(new GarageQueryCreatedEvent(id, nomGarage, mailResponsable, status, date));
    }
}
