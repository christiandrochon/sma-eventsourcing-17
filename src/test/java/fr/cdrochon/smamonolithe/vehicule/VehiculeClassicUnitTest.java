package fr.cdrochon.smamonolithe.vehicule;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.command.aggregate.VehiculeAggregate;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeBaseCommand;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.controllers.VehiculeEventController;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeEventHandler;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeEventSourcingRestController;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeSearchQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetAllVehiculesDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetImmatDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.*;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.RecursiveConversionClientVehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventHandlerService;
import fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventSourcingServiceImpl;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.cdrochon.smamonolithe.vehicule.VehiculeTestDataFactory.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeClassicUnitTest {

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private EventStore eventStore;
    @Mock
    private VehiculeCommandService vehiculeCommandService;
    @Mock
    private fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventSourcingService vehiculeEventSourcingService;
    @Mock
    private ClientEventSourcingService clientEventSourcingService;

    private AggregateTestFixture<VehiculeAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(VehiculeAggregate.class);
    }

    @Test
    void classic_01_baseCommandShouldKeepId() {
        VehiculeBaseCommand<String> command = new VehiculeBaseCommand<>("veh-100");
        assertEquals("veh-100", command.getId());
    }

    @Test
    void classic_02_createCommandShouldKeepFields() {
        Instant instant = sampleInstant();
        VehiculeCreateCommand command = new VehiculeCreateCommand("veh-1", "AA-123-BB", instant, VehiculeStatus.EN_CIRCULATION);
        assertAll(
                () -> assertEquals("veh-1", command.getId()),
                () -> assertEquals("AA-123-BB", command.getImmatriculationVehicule()),
                () -> assertEquals(instant, command.getDateMiseEnCirculationVehicule()),
                () -> assertEquals(VehiculeStatus.EN_CIRCULATION, command.getVehiculeStatus())
        );
    }

    @Test
    void classic_03_exceptionHandlerShouldReturn500() {
        VehiculeCreateCommand command = new VehiculeCreateCommand("veh", "AA", sampleInstant(), VehiculeStatus.EN_ATTENTE);
        ResponseEntity<String> response = command.exceptionHandler(new RuntimeException("boom"));
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals("boom", response.getBody())
        );
    }

    @Test
    void classic_04_createdEventShouldKeepFields() {
        VehiculeCreatedEvent event = sampleVehiculeCreatedEvent();
        assertAll(
                () -> assertEquals("veh-1", event.getId()),
                () -> assertEquals("AA-123-BB", event.getImmatriculationVehicule()),
                () -> assertEquals(sampleInstant(), event.getDateMiseEnCirculationVehicule()),
                () -> assertEquals(VehiculeStatus.EN_CIRCULATION, event.getVehiculeStatus())
        );
    }

    @Test
    void classic_05_commandDTOBuilderShouldSetAllFields() {
        VehiculeCommandDTO dto = sampleVehiculeCommandDTO();
        assertAll(
                () -> assertEquals("veh-1", dto.getId()),
                () -> assertEquals("AA-123-BB", dto.getImmatriculationVehicule()),
                () -> assertEquals(sampleInstant(), dto.getDateMiseEnCirculationVehicule()),
                () -> assertEquals(VehiculeStatus.EN_CIRCULATION, dto.getVehiculeStatus())
        );
    }

    @Test
    void classic_06_queryDTOAllArgsShouldSetFields() {
        VehiculeQueryDTO dto = new VehiculeQueryDTO("veh-2", "BB-234-CC", sampleInstant(), VehiculeStatus.VENDU, null);
        assertEquals("veh-2", dto.getId());
    }

    @Test
    void classic_07_getVehiculeDTOShouldKeepId() {
        GetVehiculeDTO dto = new GetVehiculeDTO("veh-4");
        assertEquals("veh-4", dto.getId());
    }

    @Test
    void classic_08_getImmatDTOShouldKeepImmatriculation() {
        GetImmatDTO dto = new GetImmatDTO("AB-001-CD");
        assertEquals("AB-001-CD", dto.getImmatriculation());
    }

    @Test
    void classic_09_getAllVehiculesDTOShouldInstantiate() {
        assertNotNull(new GetAllVehiculesDTO());
    }

    @Test
    void classic_10_statusForValueShouldIgnoreCase() {
        assertEquals(VehiculeStatus.EN_CIRCULATION, VehiculeStatus.forValue("en_circulation"));
    }

    @Test
    void classic_11_statusForValueShouldResolveUpperCase() {
        assertEquals(VehiculeStatus.VENDU, VehiculeStatus.forValue("VENDU"));
    }

    @Test
    void classic_12_commandServiceShouldSendCommand() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        service.createVehicule(sampleVehiculeCommandDTO());
        verify(commandGateway, times(1)).send(any(VehiculeCreateCommand.class));
    }

    @Test
    void classic_13_commandServiceShouldReturnIncompleteFuture() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        CompletableFuture<VehiculeCommandDTO> future = service.createVehicule(sampleVehiculeCommandDTO());
        assertFalse(future.isDone());
    }

    @Test
    void classic_14_commandServiceShouldCompleteFuture() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        CompletableFuture<VehiculeCommandDTO> future = service.createVehicule(sampleVehiculeCommandDTO());
        service.completeVehiculeCreation(sampleVehiculeCommandDTO());
        assertTrue(future.isDone());
    }

    @Test
    void classic_15_commandServiceCompleteWithoutFutureShouldNotThrow() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        assertDoesNotThrow(() -> service.completeVehiculeCreation(sampleVehiculeCommandDTO()));
    }

    @Test
    void classic_16_eventHandlerShouldCompleteCommandService() {
        VehiculeEventHandler handler = new VehiculeEventHandler(vehiculeCommandService);
        handler.on(sampleVehiculeCreatedEvent());
        ArgumentCaptor<VehiculeCommandDTO> captor = ArgumentCaptor.forClass(VehiculeCommandDTO.class);
        verify(vehiculeCommandService).completeVehiculeCreation(captor.capture());
        assertEquals("AA-123-BB", captor.getValue().getImmatriculationVehicule());
    }

    @Test
    void classic_17_eventSourcingServiceImplShouldDelegateReadEvents() {
        DomainEventStream stream = mock(DomainEventStream.class);
        when(eventStore.readEvents("veh-10")).thenReturn(stream);
        VehiculeEventSourcingServiceImpl service = new VehiculeEventSourcingServiceImpl(eventStore);
        assertSame(stream, service.eventsByVehiculeId("veh-10"));
    }

    @Test
    void classic_18_eventControllerShouldReturnRepositoryStream() {
        DomainEventStream stream = mock(DomainEventStream.class);
        when(vehiculeEventSourcingService.eventsByVehiculeId("veh-20")).thenReturn(stream);
        VehiculeEventController controller = new VehiculeEventController(vehiculeEventSourcingService);
        assertSame(stream, controller.eventsById("veh-20"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void classic_19_eventSourcingRestControllerShouldReturnClientEventStream() {
        DomainEventStream stream = mock(DomainEventStream.class);
        when(clientEventSourcingService.eventsByClientId("veh-30")).thenReturn(stream);
        when(stream.asStream()).thenReturn((Stream) Stream.of("evt1", "evt2"));
        VehiculeEventSourcingRestController controller = new VehiculeEventSourcingRestController(clientEventSourcingService);
        assertEquals(2L, controller.eventsByAccountId("veh-30").count());
    }

    @Test
    void classic_20_queryMapperShouldConvertSimpleVehicule() {
        VehiculeQueryDTO dto = VehiculeQueryMapper.convertVehiculeToVehiculeDTO(sampleVehicule());
        assertAll(
                () -> assertEquals("veh-1", dto.getId()),
                () -> assertEquals("AA-123-BB", dto.getImmatriculationVehicule()),
                () -> assertEquals(VehiculeStatus.EN_CIRCULATION, dto.getVehiculeStatus())
        );
    }

    @Test
    void classic_21_recursiveMapperShouldLinkClientAndVehicule() {
        var client = sampleClient();
        var vehicule = sampleVehicule("veh-90", "DD-999-EE", VehiculeStatus.EN_CIRCULATION);
        client.setVehicule(vehicule);
        vehicule.setClient(client);

        VehiculeQueryDTO vehiculeDTO = RecursiveConversionClientVehicule.addVehiculeQueryMapper(vehicule);
        assertAll(
                () -> assertNotNull(vehiculeDTO.getClient()),
                () -> assertEquals("client-1", vehiculeDTO.getClient().getId()),
                () -> assertSame(vehiculeDTO, vehiculeDTO.getClient().getVehicule())
        );
    }

    @Test
    void classic_22_eventHandlerServiceShouldSaveVehiculeOnEvent() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        service.on(sampleVehiculeCreatedEvent());
        verify(vehiculeRepository).save(any(Vehicule.class));
    }

    @Test
    void classic_23_eventHandlerServiceShouldReturnVehiculeById() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findById("veh-1")).thenReturn(Optional.of(sampleVehicule()));
        VehiculeQueryDTO dto = service.on(new GetVehiculeDTO("veh-1"));
        assertEquals("veh-1", dto.getId());
    }

    @Test
    void classic_24_eventHandlerServiceShouldReturnVehiculeByImmatriculation() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findByImmatriculationVehicule("AA-123-BB")).thenReturn(sampleVehicule());
        VehiculeQueryDTO dto = service.on(new GetImmatDTO("AA-123-BB"));
        assertEquals("AA-123-BB", dto.getImmatriculationVehicule());
    }

    @Test
    void classic_25_eventHandlerServiceShouldReturnAllVehicules() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findAll()).thenReturn(List.of(sampleVehicule()));
        assertEquals(1, service.on(new GetAllVehiculesDTO()).size());
    }

    @Test
    void classic_26_queryControllerShouldReturnVehiculeByIdAsMono() {
        VehiculeQueryController controller = new VehiculeQueryController(vehiculeRepository);
        when(vehiculeRepository.findById("veh-1")).thenReturn(Optional.of(sampleVehicule()));
        VehiculeQueryDTO dto = controller.getDocumentByIdAsync("veh-1").block();
        assertNotNull(dto);
    }

    @Test
    void classic_27_queryControllerShouldReturnFluxForAllVehicules() {
        VehiculeQueryController controller = new VehiculeQueryController(vehiculeRepository);
        when(vehiculeRepository.findAll()).thenReturn(List.of(sampleVehicule(), sampleVehicule("veh-2", "BB", VehiculeStatus.VENDU)));
        List<VehiculeQueryDTO> dtos = controller.getDossiersAsync().collectList().block();
        assertEquals(2, dtos.size());
    }

    @Test
    void classic_28_searchControllerShouldReturnImmatriculationExists() {
        VehiculeSearchQueryController controller = new VehiculeSearchQueryController(vehiculeRepository);
        when(vehiculeRepository.existsByImmatriculationVehicule("AA-123-BB")).thenReturn(Boolean.TRUE);
        assertTrue(controller.immatriculationExiste("AA-123-BB").block());
    }

    @Test
    void classic_29_searchControllerShouldReturnVehiculeByImmatriculation() {
        VehiculeSearchQueryController controller = new VehiculeSearchQueryController(vehiculeRepository);
        when(vehiculeRepository.findByImmatriculationVehicule("AA-123-BB")).thenReturn(sampleVehicule());
        ResponseEntity<?> response = controller.getVehiculeByImmatriculationAsync("AA-123-BB").block();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void classic_30_vehiculeToStringShouldContainDossierIdWhenPresent() {
        Vehicule vehicule = sampleVehicule();
        vehicule.setDossier(Dossier.builder().id("dos-1").build());
        assertTrue(vehicule.toString().contains("dos-1"));
    }

    @Test
    void classic_31_aggregateShouldPublishCreatedEvent() {
        fixture.givenNoPriorActivity()
                .when(new VehiculeCreateCommand("veh-ag-1", "AA-111-AA", sampleInstant(), VehiculeStatus.EN_ATTENTE))
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.sequenceOf(
                        org.axonframework.test.matchers.Matchers.messageWithPayload(instanceOf(VehiculeCreatedEvent.class))
                ));
    }

    @Test
    void classic_32_aggregateShouldSetStateAfterEvent() {
        fixture.givenNoPriorActivity()
                .when(new VehiculeCreateCommand("veh-ag-2", "AA-222-AA", sampleInstant(), VehiculeStatus.HORS_SERVICE))
                .expectState(aggregate -> {
                    assertEquals("veh-ag-2", aggregate.getId());
                    assertEquals("AA-222-AA", aggregate.getImmatriculationVehicule());
                    assertEquals(VehiculeStatus.HORS_SERVICE, aggregate.getVehiculeStatus());
                });
    }

    @Test
    void classic_33_typeVehiculeShouldContainVoiture() {
        assertTrue(new TypeVehicule().getTypeVehicule().contains("VOITURE"));
    }

    @Test
    void classic_34_typeCarburantShouldContainDiesel() {
        assertTrue(new TypeCarburant().getTypeCarburant().contains("DIESEL"));
    }

    @Test
    void classic_35_typeBoiteShouldContainAutomatique() {
        assertTrue(new TypeBoiteVitesse().getTypeBoiteVitesse().contains("AUTOMATIQUE"));
    }

    @Test
    void classic_36_typeDirectionShouldContainElectrique() {
        assertTrue(new TypeDirectionAssistee().getDirectionAssistee().contains("ELECTRIQUE"));
    }

    @Test
    void classic_37_typeFreinageShouldContainDisques() {
        assertTrue(new TypeFreinage().getTypeFreinage().contains("DISQUES"));
    }

    @Test
    void classic_38_typeSuspensionShouldContainClassique() {
        assertTrue(new TypeSuspension().getTypeSuspension().contains("CLASSIQUE"));
    }

    @Test
    void classic_39_typePropulsionShouldContainMoteurAvant() {
        assertTrue(new TypePropulsion().getTypePropulsion().contains("MOTEUR_A_L_AVANT"));
    }

    @Test
    void classic_40_marqueVehiculeShouldContainRenault() {
        assertTrue(new MarqueVehicule().getMarques().contains("RENAULT"));
    }

    @TestFactory
    Stream<DynamicTest> classic_dynamic_41_to_100() {
        Stream<DynamicTest> statusTests = IntStream.range(0, 20)
                .mapToObj(i -> DynamicTest.dynamicTest("classic_status_roundtrip_" + i, () -> {
                    VehiculeStatus expected = VehiculeStatus.values()[i % VehiculeStatus.values().length];
                    String input = (i % 2 == 0) ? expected.name().toLowerCase() : expected.name();
                    assertEquals(expected, VehiculeStatus.forValue(input));
                }));

        Stream<DynamicTest> collectionTests = IntStream.range(0, 10)
                .mapToObj(i -> DynamicTest.dynamicTest("classic_collection_contains_known_values_" + i, () -> {
                    Collection<String> marques = new MarqueVehicule().getMarques();
                    Collection<String> types = new TypeVehicule().getTypeVehicule();
                    assertTrue(marques.contains("NON_DISPONIBLE"));
                    assertTrue(types.contains("NON_DISPONIBLE"));
                }));

        Stream<DynamicTest> eventTests = IntStream.range(0, 20)
                .mapToObj(i -> DynamicTest.dynamicTest("classic_event_payload_" + i, () -> {
                    VehiculeCreatedEvent event = new VehiculeCreatedEvent(
                            "veh-dyn-" + i,
                            "IMM-" + i,
                            sampleInstant().plusSeconds(i),
                            VehiculeStatus.values()[i % VehiculeStatus.values().length]
                    );
                    assertEquals("veh-dyn-" + i, event.getId());
                    assertEquals("IMM-" + i, event.getImmatriculationVehicule());
                }));

        Stream<DynamicTest> commandDtoTests = IntStream.range(0, 10)
                .mapToObj(i -> DynamicTest.dynamicTest("classic_command_dto_builder_" + i, () -> {
                    VehiculeCommandDTO dto = VehiculeCommandDTO.builder()
                            .id("dto-" + i)
                            .immatriculationVehicule("AA-" + i + "-BB")
                            .dateMiseEnCirculationVehicule(sampleInstant().plusSeconds(i))
                            .vehiculeStatus(VehiculeStatus.values()[i % VehiculeStatus.values().length])
                            .build();
                    assertEquals("dto-" + i, dto.getId());
                }));

        Stream<DynamicTest> vehiculeEntityTests = IntStream.range(0, 10)
                .mapToObj(i -> DynamicTest.dynamicTest("classic_vehicule_entity_builder_" + i, () -> {
                    Vehicule vehicule = sampleVehicule("veh-ent-" + i, "IM-" + i, VehiculeStatus.EN_ATTENTE);
                    assertEquals("veh-ent-" + i, vehicule.getId());
                    assertEquals("IM-" + i, vehicule.getImmatriculationVehicule());
                }));

        return Stream.of(statusTests, collectionTests, eventTests, commandDtoTests, vehiculeEntityTests)
                .flatMap(s -> s);
    }
}

