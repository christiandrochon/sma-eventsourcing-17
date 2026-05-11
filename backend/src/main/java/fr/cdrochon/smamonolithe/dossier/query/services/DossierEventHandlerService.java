package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierListResponse;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetAllDossiersDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.query.events.DossierCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hibernate.TransactionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DossierEventHandlerService {
    
    private final DossierRepository dossierRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public DossierEventHandlerService(DossierRepository dossierRepository, ClientRepository clientRepository, VehiculeRepository vehiculeRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.dossierRepository = dossierRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service DossierQueryCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event DossierCreatedEvent event qui est declenché lors de la creation d'un dossier
     */
    @EventHandler
    @Transactional
    public void on(DossierCreatedEvent event) {
        try {
            Dossier dossier = new Dossier();
            
            
            Vehicule vehicule = new Vehicule();
            vehicule.setId(UUID.randomUUID().toString());
            vehicule.setImmatriculationVehicule(event.getVehicule().getImmatriculationVehicule());
            vehicule.setDateMiseEnCirculationVehicule(event.getVehicule().getDateMiseEnCirculationVehicule());
            vehicule.setVehiculeStatus(event.getVehicule().getVehiculeStatus());
            //SAVE vehicule en premier
            vehiculeRepository.save(vehicule);

            Client client = new Client();
            client.setId(UUID.randomUUID().toString());
            client.setNomClient(event.getClient().getNomClient());
            client.setPrenomClient(event.getClient().getPrenomClient());
            client.setAdresse(event.getClient().getAdresse());
            client.setTelClient(event.getClient().getTelClient());
            client.setMailClient(event.getClient().getMailClient());
            client.setClientStatus(event.getClient().getClientStatus());
            // lie le client avec le vehicule, mais celui-ci doit d'abord etre enregistré avant de pouvoir etre lié à un client (cle etrangere)
            client.setVehicule(vehicule);
            // SAVE
            clientRepository.save(client);

            // save les relations entre client et vehicule, mais le vehicule doit d'abord etre enregistré avant de pouvoir etre lié à un client (cle etrangere),
            // -> UPDATE le vehicule avec desormais le nouveau client qui vient d'etre enregistré
            vehicule.setClient(client);
            vehiculeRepository.save(vehicule);

            dossier.setId(event.getId());
            dossier.setNomDossier(event.getNomDossier());
            dossier.setDateCreationDossier(event.getDateCreationDossier());
            dossier.setDateModificationDossier(event.getDateModificationDossier());
            dossier.setDossierStatus(event.getDossierStatus());
            dossier.setClient(client);
            dossier.setVehicule(vehicule);
            
              //SAVE le dossier en final
              dossierRepository.save(dossier);
              BusinessLoggers.business().info("BIZ_DOSSIER_CREATED dossierId={} nomDossier={} clientId={} vehiculeId={} status={}",
                                              dossier.getId(),
                                              dossier.getNomDossier(),
                                              client.getId(),
                                              vehicule.getId(),
                                              dossier.getDossierStatus());

              // Publier un événement Spring APRÈS la transaction pour notifier les listeners (notamment DossierCommandService)
              final DossierQueryDTO dossierDTO = DossierQueryMapper.convertDossierToDossierDTO(dossier);
              if (TransactionSynchronizationManager.isSynchronizationActive()) {
                  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                      @Override
                      public void afterCommit() {
                          applicationEventPublisher.publishEvent(new DossierCreatedApplicationEvent(this, dossierDTO));
                      }
                  });
              } else {
                  // Si pas de synchronisation active, publier immédiatement
                  applicationEventPublisher.publishEvent(new DossierCreatedApplicationEvent(this, dossierDTO));
              }


        } catch(Exception e) {
            log.error("TECH_DOSSIER_PERSIST_ERROR dossierId={} message={}", event.getId(), e.getMessage(), e);
            throw new TransactionException("Erreur lors de la creation du dossier : " + e.getMessage());
        }
    }
    
    /**
     * Recupere un dossier avec son id
     *
     * @param getDossierDTO DTO contenant l'id du dossier a recuperer
     * @return DossierResponseDTO contenant les informations du dossier
     */
    @QueryHandler
    public DossierQueryDTO on(GetDossierDTO getDossierDTO) {
        return dossierRepository.findById(getDossierDTO.getId()).map(DossierQueryMapper::convertDossierToDossierDTO)
                                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));
    }
    
    /**
     * Recupere tous les dossiers
     *
     * @param query requete Axon de recuperation de tous les dossiers
     * @return DossierListResponse contenant la liste des dossiers
     */
    @QueryHandler
    public DossierListResponse on(GetAllDossiersDTO query) {
        List<Dossier> dossiers = dossierRepository.findAll();
        List<DossierQueryDTO> dtos = dossiers.stream().map(DossierQueryMapper::convertDossierToDossierDTO).collect(Collectors.toList());
        return new DossierListResponse(dtos);
    }

    // Compatibilite tests/unit legacy: conserve la signature historique sans argument.
    public List<DossierQueryDTO> on() {
        return on(new GetAllDossiersDTO()).getItems();
    }
}
