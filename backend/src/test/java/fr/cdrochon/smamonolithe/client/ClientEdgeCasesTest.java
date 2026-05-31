package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.client.query.services.ClientEventHandlerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 25 tests de cas limites pour la couche Client (événements, commandes, entités, service).
 */
/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class ClientEdgeCasesTest {

    /**
     * ── Cas limites : ClientCreatedEvent ────────────────────────────────────────
     */

    @Test
    @DisplayName("EC-01 : ClientCreatedEvent conserve l'id fourni")
    void event_shouldPreserveId() {
        ClientCreatedEvent event = sampleClientCreatedEvent();
        assertEquals("client-uuid-1", event.getId());
    }

    @Test
    @DisplayName("EC-02 : ClientCreatedEvent conserve le nom du client")
    void event_shouldPreserveNom() {
        assertEquals("Dupont", sampleClientCreatedEvent().getNomClient());
    }

    @Test
    @DisplayName("EC-03 : ClientCreatedEvent conserve le prénom du client")
    void event_shouldPreservePrenom() {
        assertEquals("Jean", sampleClientCreatedEvent().getPrenomClient());
    }

    @Test
    @DisplayName("EC-04 : ClientCreatedEvent conserve l'email du client")
    void event_shouldPreserveMail() {
        assertEquals("jean.dupont@mail.com", sampleClientCreatedEvent().getMailClient());
    }

    @Test
    @DisplayName("EC-05 : ClientCreatedEvent conserve le téléphone du client")
    void event_shouldPreserveTel() {
        assertEquals("0601020304", sampleClientCreatedEvent().getTelClient());
    }

    @Test
    @DisplayName("EC-06 : ClientCreatedEvent conserve le statut ACTIF")
    void event_shouldPreserveStatusActif() {
        assertEquals(ClientStatus.ACTIF, sampleClientCreatedEvent().getClientStatus());
    }

    @Test
    @DisplayName("EC-07 : ClientCreatedEvent avec statut HISTORISE conserve HISTORISE")
    void event_shouldAllowHistoriseStatus() {
        ClientCreatedEvent event = new ClientCreatedEvent(
                "client-uuid-hist", "Martin", "Paul",
                "p.martin@mail.com", "0699999999",
                sampleAdresseClient(), ClientStatus.HISTORISE);
        assertEquals(ClientStatus.HISTORISE, event.getClientStatus());
    }

    @Test
    @DisplayName("EC-08 : ClientCreatedEvent conserve l'adresse (ville)")
    void event_shouldPreserveAdresseVille() {
        assertEquals("Paris", sampleClientCreatedEvent().getAdresseClient().getVille());
    }

    /**
     * ── Cas limites : AdresseClient ─────────────────────────────────────────────
     */

    @Test
    @DisplayName("EC-09 : AdresseClient créé via builder conserve le pays FRANCE")
    void adresse_builderShouldPreservePays() {
        AdresseClient adresse = sampleAdresseClient();
        assertEquals(Pays.FRANCE, adresse.getPays());
    }

    @Test
    @DisplayName("EC-10 : AdresseClient copié depuis ClientAdresseDTO a le même code postal")
    void adresse_copyConstructorShouldPreserveCp() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        AdresseClient adresse = new AdresseClient(dto);
        assertEquals(dto.getCp(), adresse.getCp());
    }

    @Test
    @DisplayName("EC-11 : AdresseClient copié depuis DTO a la même rue")
    void adresse_copyConstructorShouldPreserveRue() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        AdresseClient adresse = new AdresseClient(dto);
        assertEquals(dto.getRue(), adresse.getRue());
    }

    /**
     * ── Cas limites : Client (entité JPA) ───────────────────────────────────────
     */

    @Test
    @DisplayName("EC-12 : Client.toString() ne lève pas d'exception quand dossier est null")
    void client_toStringShouldNotThrowWhenDossierIsNull() {
        Client client = sampleClient();
        assertDoesNotThrow(client::toString);
    }

    @Test
    @DisplayName("EC-13 : Client.toString() contient l'id du client")
    void client_toStringShouldContainId() {
        Client client = sampleClient();
        assertTrue(client.toString().contains("client-uuid-1"));
    }

    @Test
    @DisplayName("EC-14 : Client.toString() affiche 'null' quand dossier est null")
    void client_toStringShouldShowNullForDossier() {
        Client client = sampleClient();
        assertTrue(client.toString().contains("null"));
    }

    @Test
    @DisplayName("EC-15 : Client sans adresse ne lève pas d'exception à la construction")
    void client_builderWithNullAdressShouldNotThrow() {
        assertDoesNotThrow(() -> Client.builder()
                .id("id-sans-adresse")
                .nomClient("Toto")
                .build());
    }

    /**
     * ── Cas limites : GetClientDTO ───────────────────────────────────────────────
     */

    @Test
    @DisplayName("EC-16 : GetClientDTO conserve l'id")
    void getClientDTO_shouldPreserveId() {
        GetClientDTO dto = new GetClientDTO("abc-123");
        assertEquals("abc-123", dto.getId());
    }

    @Test
    @DisplayName("EC-17 : GetClientDTO avec id null conserve null")
    void getClientDTO_shouldAcceptNullId() {
        GetClientDTO dto = new GetClientDTO(null);
        assertNull(dto.getId());
    }

    /**
     * ── Cas limites : ClientEventHandlerService ──────────────────────────────────
     */

    @Mock
    private ClientRepository clientRepository;

    private ClientEventHandlerService service;

    @BeforeEach
    void setUp() {
        service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("EC-18 : save() appelé exactement une fois par événement ClientCreated")
    void service_saveShouldBeCalledOnce() {
        service.on(sampleClientCreatedEvent());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("EC-19 : Le client sauvegardé possède l'id de l'événement")
    void service_savedClientShouldHaveEventId() {
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(sampleClientCreatedEvent());
        verify(clientRepository).save(captor.capture());
        assertEquals("client-uuid-1", captor.getValue().getId());
    }

    @Test
    @DisplayName("EC-20 : Le client sauvegardé possède le prénom de l'événement")
    void service_savedClientShouldHaveEventPrenom() {
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(sampleClientCreatedEvent());
        verify(clientRepository).save(captor.capture());
        assertEquals("Jean", captor.getValue().getPrenomClient());
    }

    @Test
    @DisplayName("EC-21 : Le client sauvegardé possède l'email de l'événement")
    void service_savedClientShouldHaveEventMail() {
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(sampleClientCreatedEvent());
        verify(clientRepository).save(captor.capture());
        assertEquals("jean.dupont@mail.com", captor.getValue().getMailClient());
    }

    @Test
    @DisplayName("EC-22 : Le client sauvegardé possède le téléphone de l'événement")
    void service_savedClientShouldHaveEventTel() {
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(sampleClientCreatedEvent());
        verify(clientRepository).save(captor.capture());
        assertEquals("0601020304", captor.getValue().getTelClient());
    }

    @Test
    @DisplayName("EC-23 : findAll retournant plusieurs clients retourne la même taille")
    void service_findAllShouldReturnSameSizeAsList() {
        Client c1 = sampleClient();
        Client c2 = Client.builder()
                .id("client-uuid-2").nomClient("Durand").prenomClient("Marie")
                .mailClient("m.durand@mail.com").telClient("0600000001")
                .adresse(sampleAdresseClient()).clientStatus(ClientStatus.ACTIF).build();
        when(clientRepository.findAll()).thenReturn(List.of(c1, c2));
        assertEquals(2, service.on().size());
    }

    @Test
    @DisplayName("EC-24 : Exception NullPointerException sur save est avalée (pas de propagation)")
    void service_shouldSwallowNullPointerException() {
        when(clientRepository.save(any(Client.class))).thenThrow(new NullPointerException("npe"));
        assertDoesNotThrow(() -> service.on(sampleClientCreatedEvent()));
    }

    @Test
    @DisplayName("EC-25 : findById avec id vide lève EntityNotFoundException")
    void service_shouldThrowForEmptyStringId() {
        when(clientRepository.findById("")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.on(new GetClientDTO("")));
    }
}

