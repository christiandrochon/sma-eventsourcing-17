package fr.cdrochon.smamonolithe.vehicule.command.aggregate;

import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

@Aggregate @Getter @Setter
public class VehiculeAggregate {
    
    @AggregateIdentifier
    private String id;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    //    private Instant dateDeValiditeControleTechnique;
    //    private Instant dateValiditeControleTechniqueComplementaire;
    //    private String urlCertificatImmatriculation;
    //    private String modeleVehicule;
    //    private String versionVehicule;
    //    @Embedded private MarqueVehicule marqueVehicule;
    //    @Embedded private MotorisationVehicule motorisationVehicule;
    //    @Embedded private TypeCarburant typeCarburant;
    //    @Embedded private TypeBoiteVitesse typeBoiteVitesse;
    //    @Embedded private TypeDirectionAssistee typeDirectionAssistee;
    //    @Embedded private TypeFreinage typeFreinage;
    //    @Embedded private TypePropulsion typePropulsion;
    //    @Embedded private TypeSuspension typeSuspension;
    //    @Embedded private TypeVehicule typeVehicule;
    //    private String finitionMotorisationVehicule;
    //    private int puissanceFiscaleVehicule;
    //    private int puissanceVehicule;
    //    private int nombrePortesVehicule;
    //    private int nombrePlacesVehicule;
    //    private int kilometrageVehicule;
    //    private int anneeVehicule;
    //    private String couleurVehicule;
    //    private String urlPhotoVehicule;
    //    private boolean climatisationVehicule;
    
    private VehiculeStatus vehiculeStatus;
    
    
    public VehiculeAggregate() {
        //requis par Axon
    }
    
    /**
     * FONCTION DE DECISION (regle metier)
     * <p>
     * Publiation d'un event via AggregateLifeCycle.apply(). Normalement, cet event devrait etre enregistré dans l'event store
     * <p>
     * Instancie un nouvel agregat à chaque requete recue
     * ici => fonction de decision = verifie regle metier
     *
     * @param createVehiculeCommand Commande de creation d'un vehicule
     */
    @CommandHandler
    public VehiculeAggregate(VehiculeCreateCommand createVehiculeCommand) {
        
        //ici => fonction de decision = verifie regle metier
        if(createVehiculeCommand.getImmatriculationVehicule() == null) {
            throw new CreatedGarageException("Le vehicule doit exister ! ");
        }
        
        System.out.println("**************************");
        System.out.println("Publication de l'evenement = commandHandler dans aggregate");
        AggregateLifecycle.apply(new VehiculeCreatedEvent(createVehiculeCommand.getId(),
                                                          createVehiculeCommand.getImmatriculationVehicule(),
                                                          createVehiculeCommand.getDateMiseEnCirculationVehicule(),
                                                          createVehiculeCommand.getVehiculeStatus()
        ));
        System.out.println("**************************");
    }
    
    /**
     * FONCTION D'EVOLUTION (muter l'etat de l'agregat)
     * <p>
     * Pour chaque event de type VehiculeCreatedEvent qui arrive dans l'eventstore, on va muter l'etat de l'application
     *
     * @param event Event de mutation d'un vehicule
     */
    @EventSourcingHandler
    public void on(VehiculeCreatedEvent event) {
        
        System.out.println("**********************");
        System.out.println("Agregat Enventsourcinghandler ");
        this.id = event.getId();
        this.immatriculationVehicule = event.getImmatriculationVehicule();
        this.dateMiseEnCirculationVehicule = event.getDateMiseEnCirculationVehicule();
        this.vehiculeStatus = event.getVehiculeStatus();
    }
}
