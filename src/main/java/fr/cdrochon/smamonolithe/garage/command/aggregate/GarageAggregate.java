package fr.cdrochon.smamonolithe.garage.command.aggregate;

import fr.cdrochon.smamonolithe.garage.command.commands.GarageCreateCommand;
import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.garage.events.GarageCreatedEvent;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

/**
 * Un agrégat correspond à l'état stable courant de l'application.
 */
@Aggregate
@Slf4j
public class GarageAggregate {
    
    @AggregateIdentifier
    private String id;
    private String nomGarage;
    private String mailResponsable;
    private AdresseGarage adresseGarage;
    private GarageStatus status;
    
    public GarageAggregate() {
        // required by Axon
    }
    
    
    /**
     * <str>Fonction de décision</str> = vérifie règle métier
     * <p>
     * Instancie un nouvel agrégat à chaque requête reçue dans le bus de commande via le @CommandHandler qui subscribe au commandBus.
     * <p>
     * AggregateLifecycle.apply() = publication de l'event
     *
     * @param command Commande de création d'un garage
     */
    @CommandHandler
    public GarageAggregate(GarageCreateCommand command) {
        //ici => fonction de decision = verifie regle metier
        if(command.getNomGarage() == null) {
            throw new CreatedGarageException("Le nom du garage doit etre renseigné ! ");
        }
        
        //conversion dto à entity
        AdresseGarage adresseGarage = new AdresseGarage(command.getAdresse().getNumeroDeRue(),
                                                        command.getAdresse().getRue(),
                                                        command.getAdresse().getCp(),
                                                        command.getAdresse().getVille());
        
        //publication de l'event
        AggregateLifecycle.apply(
                new GarageCreatedEvent(command.getId(),
                                       command.getNomGarage(),
                                       command.getMailResp(),
                                       adresseGarage,
                                       GarageStatus.CREATED
                ));
        
    }
    
    
    /**
     * <str>Fonction d'évolution</str> = met à jour l'etat de l'agregat
     *
     * @param event Event de creation d'un garage
     */
    @EventSourcingHandler
    public void on(GarageCreatedEvent event) {
        
        log.info("Event sourced : {}", event);
        this.id = event.getId();
        this.nomGarage = event.getNomGarage();
        this.mailResponsable = event.getMailResponsable();
        this.adresseGarage = event.getAdresseGarage();
        this.status = event.getGarageStatus();
    }
    
}
