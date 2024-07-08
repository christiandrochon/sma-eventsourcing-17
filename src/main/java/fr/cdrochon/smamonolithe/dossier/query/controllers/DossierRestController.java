package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierResponseDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.query.mapper.DossierMapper;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class DossierRestController {
    
    private final QueryGateway queryGateway;
    private final DossierRepository dossierRepository;
    
    public DossierRestController(QueryGateway queryGateway, DossierRepository dossierRepository) {
        this.queryGateway = queryGateway;
        this.dossierRepository = dossierRepository;
    }
    
    /**
     * Renvoi les informations utiles à la partie query lors d'une recherche. Il y a moins d'informations que dans l'objet renvoyé pour l'affichage d'un
     * client à un concessionnaire
     *
     * @param id id du dossier
     * @return DossierResponseDTO avec les informations utiles pour la partie query
     */
    @GetMapping(path = "/dossiers/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public DossierResponseDTO getDossier(@PathVariable String id) {
        GetDossierDTO dossierDTO = new GetDossierDTO();
        dossierDTO.setId(id);
        return queryGateway.query(dossierDTO, DossierResponseDTO.class).join();
    }
    
    /**
     * Pur trouver tous les dossiers, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO
     * sous forme de multiples instances
     *
     * @return List<DossierResponseDTO> liste des dossiers sous forme de DTO
     */
    @GetMapping(path = "/dossiers")
    //    @PreAuthorize("hasAuthority('USER')")
    public List<DossierResponseDTO> getAll() {
        
        List<Dossier> dossiers = dossierRepository.findAll();
        List<DossierResponseDTO> dossiersResponseDTO = dossiers.stream()
                                                               .map(DossierMapper::convertDossierToDossierDTO)
                                                               .collect(Collectors.toList());
        System.out.println("dossiersResponseDTO = " + dossiersResponseDTO);
        return dossiers.stream()
                       .map(DossierMapper::convertDossierToDossierDTO)
                       .collect(Collectors.toList());
    }
    
    
    /**
     * Renvoi un flux de DossierResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du dossier
     * @return Flux de DossierResponseDTO
     */
    @GetMapping(value = "/dossier/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DossierResponseDTO> watch(@PathVariable String id) {
        
        try(SubscriptionQueryResult<DossierResponseDTO, DossierResponseDTO> result = queryGateway.subscriptionQuery(
                new GetDossierDTO(id),
                ResponseTypes.instanceOf(DossierResponseDTO.class),
                ResponseTypes.instanceOf(DossierResponseDTO.class))) {
            return result.initialResult().concatWith(result.updates());
        }
    }
}
