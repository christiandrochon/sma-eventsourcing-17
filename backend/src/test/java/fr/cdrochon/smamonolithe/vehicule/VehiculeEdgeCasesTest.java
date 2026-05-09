package fr.cdrochon.smamonolithe.vehicule;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.command.aggregate.VehiculeAggregate;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.controllers.VehiculeCommandController;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeSearchQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetAllVehiculesDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetImmatDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.*;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.RecursiveConversionClientVehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventHandlerService;
import jakarta.persistence.EntityNotFoundException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.hibernate.TransactionException;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.cdrochon.smamonolithe.vehicule.VehiculeTestDataFactory.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculeEdgeCasesTest {

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private VehiculeCommandService vehiculeCommandService;

    private AggregateTestFixture<VehiculeAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(VehiculeAggregate.class);
    }

    @Test
    void edge_01_statusUnknownShouldFallbackToEnAttente() {
        assertEquals(VehiculeStatus.EN_ATTENTE, VehiculeStatus.forValue("UNKNOWN_STATUS"));
    }

    @Test
    void edge_02_statusEmptyShouldFallbackToEnAttente() {
        assertEquals(VehiculeStatus.EN_ATTENTE, VehiculeStatus.forValue(""));
    }

    @Test
    void edge_03_statusNullShouldFallbackToEnAttente() {
        assertEquals(VehiculeStatus.EN_ATTENTE, VehiculeStatus.forValue(null));
    }

    @Test
    void edge_04_aggregateShouldThrowWhenImmatriculationIsNull() {
        fixture.givenNoPriorActivity()
                .when(new VehiculeCreateCommand("veh-agg-null", null, sampleInstant(), VehiculeStatus.EN_ATTENTE))
                .expectException(CreatedGarageException.class)
                .expectExceptionMessage(containsString("vehicule"));
    }

    @Test
    void edge_05_aggregateShouldAllowEmptyImmatriculation() {
        fixture.givenNoPriorActivity()
                .when(new VehiculeCreateCommand("veh-agg-empty", "", sampleInstant(), VehiculeStatus.EN_ATTENTE))
                .expectSuccessfulHandlerExecution();
    }

    @Test
    void edge_06_eventHandlerServiceShouldThrowTransactionExceptionOnSaveFailure() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.save(any(Vehicule.class))).thenThrow(new RuntimeException("db down"));
        assertThrows(TransactionException.class, () -> service.on(sampleVehiculeCreatedEvent()));
    }

    @Test
    void edge_07_eventHandlerServiceShouldThrowEntityNotFoundForUnknownId() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.on(new GetVehiculeDTO("unknown")));
    }

    @Test
    void edge_08_eventHandlerServiceShouldThrowEntityNotFoundForUnknownImmat() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findByImmatriculationVehicule("not-found")).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> service.on(new GetImmatDTO("not-found")));
    }

    @Test
    void edge_09_searchControllerShouldReturn404WhenImmatNotFound() {
        VehiculeSearchQueryController controller = new VehiculeSearchQueryController(vehiculeRepository);
        when(vehiculeRepository.findByImmatriculationVehicule("none")).thenReturn(null);
        ResponseEntity<?> response = controller.getVehiculeByImmatriculationAsync("none").block();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void edge_10_searchControllerShouldReturn500WhenRepositoryThrows() {
        VehiculeSearchQueryController controller = new VehiculeSearchQueryController(vehiculeRepository);
        when(vehiculeRepository.findByImmatriculationVehicule("error")).thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> response = controller.getVehiculeByImmatriculationAsync("error").block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void edge_11_commandControllerShouldReturn500OnFutureError() {
        VehiculeCommandController controller = new VehiculeCommandController(vehiculeCommandService);
        CompletableFuture<VehiculeCommandDTO> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("failure"));
        when(vehiculeCommandService.createVehicule(any(VehiculeCommandDTO.class))).thenReturn(failedFuture);

        ResponseEntity<VehiculeCommandDTO> response = controller.createClientAsync(sampleVehiculeCommandDTO()).block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void edge_12_commandServiceShouldThrowWhenDtoIsNull() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        assertThrows(NullPointerException.class, () -> service.createVehicule(null));
    }

    @Test
    void edge_13_commandServiceShouldSendCommandEvenWithNullFieldValues() {
        VehiculeCommandService service = new VehiculeCommandService(commandGateway);
        VehiculeCommandDTO dto = VehiculeCommandDTO.builder().id("x").immatriculationVehicule(null).dateMiseEnCirculationVehicule(null).vehiculeStatus(null).build();
        service.createVehicule(dto);
        verify(commandGateway).send(any(VehiculeCreateCommand.class));
    }

    @Test
    void edge_14_recursiveMapperShouldHandleVehiculeWithoutClient() {
        VehiculeQueryDTO dto = RecursiveConversionClientVehicule.addVehiculeQueryMapper(sampleVehicule("veh-no-client", "X", VehiculeStatus.EN_ATTENTE));
        assertNull(dto.getClient());
    }

    @Test
    void edge_15_recursiveMapperShouldHandleClientWithoutVehicule() {
        var dto = RecursiveConversionClientVehicule.addClientQueryMapper(sampleClient());
        assertNull(dto.getVehicule());
    }

    @Test
    void edge_16_vehiculeToStringShouldContainNullWhenNoDossier() {
        assertTrue(sampleVehicule().toString().contains("null"));
    }

    @Test
    void edge_17_vehiculeBuilderShouldAllowNullId() {
        Vehicule vehicule = Vehicule.builder().id(null).immatriculationVehicule("AA").build();
        assertNull(vehicule.getId());
    }

    @Test
    void edge_18_queryControllerShouldReturnNullWhenVehiculeNotFound() {
        VehiculeQueryController controller = new VehiculeQueryController(vehiculeRepository);
        when(vehiculeRepository.findById("none")).thenReturn(Optional.empty());
        assertNull(controller.getDocumentByIdAsync("none").block());
    }

    @Test
    void edge_19_queryControllerShouldReturnEmptyFluxWhenNoVehicule() {
        VehiculeQueryController controller = new VehiculeQueryController(vehiculeRepository);
        when(vehiculeRepository.findAll()).thenReturn(List.of());
        assertEquals(0, controller.getDossiersAsync(null).collectList().block().size());
    }

    @Test
    void edge_20_eventHandlerServiceShouldSaveNullStatusAsIs() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        VehiculeCreatedEvent event = new VehiculeCreatedEvent("veh-null-status", "AA", sampleInstant(), null);
        service.on(event);
        ArgumentCaptor<Vehicule> captor = ArgumentCaptor.forClass(Vehicule.class);
        verify(vehiculeRepository).save(captor.capture());
        assertNull(captor.getValue().getVehiculeStatus());
    }

    @Test
    void edge_21_eventHandlerServiceShouldSaveNullDateAsIs() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        VehiculeCreatedEvent event = new VehiculeCreatedEvent("veh-null-date", "AA", null, VehiculeStatus.EN_ATTENTE);
        service.on(event);
        ArgumentCaptor<Vehicule> captor = ArgumentCaptor.forClass(Vehicule.class);
        verify(vehiculeRepository).save(captor.capture());
        assertNull(captor.getValue().getDateMiseEnCirculationVehicule());
    }

    @Test
    void edge_22_createCommandShouldAllowNullStatus() {
        VehiculeCreateCommand command = new VehiculeCreateCommand("veh", "AA", sampleInstant(), null);
        assertNull(command.getVehiculeStatus());
    }

    @Test
    void edge_23_createCommandShouldAllowNullDate() {
        VehiculeCreateCommand command = new VehiculeCreateCommand("veh", "AA", null, VehiculeStatus.EN_ATTENTE);
        assertNull(command.getDateMiseEnCirculationVehicule());
    }

    @Test
    void edge_24_getVehiculeDTOShouldAcceptNullId() {
        GetVehiculeDTO dto = new GetVehiculeDTO(null);
        assertNull(dto.getId());
    }

    @Test
    void edge_25_getImmatDTOShouldAcceptNullImmatriculation() {
        GetImmatDTO dto = new GetImmatDTO(null);
        assertNull(dto.getImmatriculation());
    }

    @Test
    void edge_26_marqueVehiculeShouldAllowCustomCollection() {
        MarqueVehicule marque = new MarqueVehicule();
        marque.setMarques(List.of("CUSTOM"));
        assertEquals(1, marque.getMarques().size());
    }

    @Test
    void edge_27_typeVehiculeShouldAllowEmptyCollection() {
        TypeVehicule type = new TypeVehicule();
        type.setTypeVehicule(List.of());
        assertTrue(type.getTypeVehicule().isEmpty());
    }

    @Test
    void edge_28_typeSuspensionShouldAllowNullCollection() {
        TypeSuspension type = new TypeSuspension();
        type.setTypeSuspension(null);
        assertNull(type.getTypeSuspension());
    }

    @Test
    void edge_29_typeFreinageShouldAllowDuplicates() {
        TypeFreinage type = new TypeFreinage();
        type.setTypeFreinage(List.of("DISQUES", "DISQUES"));
        assertEquals(2, type.getTypeFreinage().size());
    }

    @Test
    void edge_30_typeDirectionShouldContainSansDirectionAssistee() {
        assertTrue(new TypeDirectionAssistee().getDirectionAssistee().contains("SANS_DIRECTION_ASSISTEE"));
    }

    @Test
    void edge_31_eventHandlerServiceShouldReturnEmptyListWhenRepositoryEmpty() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.findAll()).thenReturn(List.of());
        assertTrue(service.on(new GetAllVehiculesDTO()).isEmpty());
    }

    @Test
    void edge_32_commandControllerShouldReturnCreatedWhenFutureCompletes() {
        VehiculeCommandController controller = new VehiculeCommandController(vehiculeCommandService);
        when(vehiculeCommandService.createVehicule(any(VehiculeCommandDTO.class))).thenReturn(CompletableFuture.completedFuture(sampleVehiculeCommandDTO()));
        ResponseEntity<VehiculeCommandDTO> response = controller.createClientAsync(sampleVehiculeCommandDTO()).block();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void edge_33_recursiveMapperShouldReuseSameVehiculeDtoForSameId() {
        Vehicule vehicule = sampleVehicule("veh-same", "AA", VehiculeStatus.EN_ATTENTE);
        VehiculeQueryDTO dto1 = RecursiveConversionClientVehicule.addVehiculeQueryMapper(vehicule);
        VehiculeQueryDTO dto2 = RecursiveConversionClientVehicule.addVehiculeQueryMapper(vehicule);
        assertSame(dto1, dto2);
    }

    @Test
    void edge_34_recursiveMapperShouldReuseSameClientDtoForSameId() {
        var client = sampleClient();
        var dto1 = RecursiveConversionClientVehicule.addClientQueryMapper(client);
        var dto2 = RecursiveConversionClientVehicule.addClientQueryMapper(client);
        assertSame(dto1, dto2);
    }

    @Test
    void edge_35_eventHandlerServiceShouldThrowTransactionExceptionForAnySaveError() {
        VehiculeEventHandlerService service = new VehiculeEventHandlerService(vehiculeRepository);
        when(vehiculeRepository.save(any(Vehicule.class))).thenThrow(new IllegalStateException("state"));
        assertThrows(TransactionException.class, () -> service.on(sampleVehiculeCreatedEvent()));
    }

    @TestFactory
    Stream<DynamicTest> edge_dynamic_36_to_100() {
        Stream<DynamicTest> unknownStatusTests = IntStream.range(0, 30)
                .mapToObj(i -> DynamicTest.dynamicTest("edge_unknown_status_fallback_" + i, () -> {
                    assertEquals(VehiculeStatus.EN_ATTENTE, VehiculeStatus.forValue("unknown-" + i));
                }));

        Stream<DynamicTest> createCommandStringTests = IntStream.range(0, 20)
                .mapToObj(i -> DynamicTest.dynamicTest("edge_create_command_weird_immat_" + i, () -> {
                    String immat = switch (i % 5) {
                        case 0 -> "";
                        case 1 -> "   ";
                        case 2 -> "@@@" + i;
                        case 3 -> "\n" + i;
                        default -> "A".repeat(32) + i;
                    };
                    VehiculeCreateCommand command = new VehiculeCreateCommand("id-" + i, immat, null, null);
                    assertEquals(immat, command.getImmatriculationVehicule());
                }));

        Stream<DynamicTest> dtoNullFieldTests = IntStream.range(0, 15)
                .mapToObj(i -> DynamicTest.dynamicTest("edge_dto_null_fields_" + i, () -> {
                    VehiculeQueryDTO dto = new VehiculeQueryDTO();
                    if (i % 2 == 0) {
                        dto.setId("veh-q-" + i);
                    }
                    if (i % 3 == 0) {
                        dto.setImmatriculationVehicule("IMM-" + i);
                    }
                    dto.setDateMiseEnCirculationVehicule(null);
                    dto.setVehiculeStatus(null);
                    assertNull(dto.getDateMiseEnCirculationVehicule());
                    assertNull(dto.getVehiculeStatus());
                }));

        return Stream.of(unknownStatusTests, createCommandStringTests, dtoNullFieldTests)
                .flatMap(s -> s);
    }
}

