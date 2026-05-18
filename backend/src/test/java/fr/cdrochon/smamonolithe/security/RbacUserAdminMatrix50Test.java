package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import fr.cdrochon.smamonolithe.document.command.controllers.DocumentCommandController;
import fr.cdrochon.smamonolithe.document.query.controllers.DocumentQueryController;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import fr.cdrochon.smamonolithe.dossier.command.controller.DossierCommandController;
import fr.cdrochon.smamonolithe.dossier.query.controllers.DossierQueryController;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetAllDossiersDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierListResponse;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.command.controllers.VehiculeCommandController;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import fr.cdrochon.smamonolithe.client.command.controller.ClientCommandController;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RbacUserAdminMatrix50Test {

    @TestFactory
    List<DynamicTest> rbacMatrix50() throws Exception {
        List<DynamicTest> tests = new ArrayList<>();

        // ===========================
        // 1) Document access (14)
        // ===========================
        tests.add(DynamicTest.dynamicTest("DOC-01 admin voit toute la liste", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findAll()).thenReturn(List.of(doc("d1", "u1@mail"), doc("d2", "u2@mail"), doc("d3", null)));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(auth("admin@mail", "ADMIN")).collectList())
                    .assertNext(list -> assertEquals(3, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-02 user voit seulement ses docs", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findByClientMailClient("u1@mail")).thenReturn(List.of(doc("d1", "u1@mail"), doc("d4", "u1@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(auth("u1@mail", "USER")).collectList())
                    .assertNext(list -> assertEquals(2, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-03 user2 voit ses docs", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findByClientMailClient("u2@mail")).thenReturn(List.of(doc("d2", "u2@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(auth("u2@mail", "USER")).collectList())
                    .assertNext(list -> assertEquals(1, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-04 user sans email interdit (liste)", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(authNoEmail("USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-05 auth null interdit (liste)", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(null))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-06 admin lit doc d'un autre", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d2")).thenReturn(java.util.Optional.of(doc("d2", "u2@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d2", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("d2", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-07 user lit son doc", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d1")).thenReturn(java.util.Optional.of(doc("d1", "u1@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d1", auth("u1@mail", "USER")))
                    .assertNext(dto -> assertEquals("d1", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-08 user lit doc autre client -> 403", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d2")).thenReturn(java.util.Optional.of(doc("d2", "u2@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d2", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-09 user sans email interdit (unitaire)", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d1")).thenReturn(java.util.Optional.of(doc("d1", "u1@mail")));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d1", authNoEmail("USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-10 doc introuvable -> null", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("absent")).thenReturn(java.util.Optional.empty());
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("absent", auth("admin@mail", "ADMIN")))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-11 liste user appelle findByClientMailClient", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findByClientMailClient("u1@mail")).thenReturn(List.of());
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(auth("u1@mail", "USER")).collectList())
                    .assertNext(list -> assertTrue(list.isEmpty()))
                    .verifyComplete();
            verify(repo, times(1)).findByClientMailClient("u1@mail");
        }));
        tests.add(DynamicTest.dynamicTest("DOC-12 liste admin n'appelle pas findByClientMailClient", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findAll()).thenReturn(List.of());
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentsAsync(auth("admin@mail", "ADMIN")).collectList())
                    .assertNext(list -> assertTrue(list.isEmpty()))
                    .verifyComplete();
            verify(repo, never()).findByClientMailClient(any());
        }));
        tests.add(DynamicTest.dynamicTest("DOC-13 admin lit doc sans propriétaire", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d3")).thenReturn(java.util.Optional.of(doc("d3", null)));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d3", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("d3", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOC-14 user lit doc sans propriétaire -> 403", () -> {
            DocumentRepository repo = mock(DocumentRepository.class);
            when(repo.findById("d3")).thenReturn(java.util.Optional.of(doc("d3", null)));
            DocumentQueryController c = new DocumentQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("d3", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));

        // ===========================
        // 2) Vehicule access (14)
        // ===========================
        tests.add(DynamicTest.dynamicTest("VEH-01 admin voit tous les véhicules", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findAll()).thenReturn(List.of(veh("v1", "u1@mail"), veh("v2", "u2@mail"), veh("v3", null)));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getDossiersAsync(auth("admin@mail", "ADMIN")).collectList())
                    .assertNext(list -> assertEquals(3, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-02 user voit seulement ses véhicules", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findByClientMailClient("u1@mail")).thenReturn(List.of(veh("v1", "u1@mail"), veh("v4", "u1@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getDossiersAsync(auth("u1@mail", "USER")).collectList())
                    .assertNext(list -> assertEquals(2, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-03 user sans email interdit (liste)", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            VehiculeQueryController c = new VehiculeQueryController(repo);
            java.util.concurrent.CompletionException ex = assertThrows(java.util.concurrent.CompletionException.class,
                    () -> c.getDossiersAsync(authNoEmail("USER")));
            assertTrue(ex.getCause() instanceof ResponseStatusException);
            assertEquals(403, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
        }));
        tests.add(DynamicTest.dynamicTest("VEH-04 auth null interdite (liste)", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            VehiculeQueryController c = new VehiculeQueryController(repo);
            java.util.concurrent.CompletionException ex = assertThrows(java.util.concurrent.CompletionException.class,
                    () -> c.getDossiersAsync(null));
            assertTrue(ex.getCause() instanceof ResponseStatusException);
            assertEquals(403, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
        }));
        tests.add(DynamicTest.dynamicTest("VEH-05 admin lit véhicule autre client", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v2")).thenReturn(java.util.Optional.of(veh("v2", "u2@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v2", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("v2", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-06 user lit son véhicule", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v1")).thenReturn(java.util.Optional.of(veh("v1", "u1@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v1", auth("u1@mail", "USER")))
                    .assertNext(dto -> assertEquals("v1", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-07 user lit véhicule autre client -> 403", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v2")).thenReturn(java.util.Optional.of(veh("v2", "u2@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v2", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-08 user sans email interdit (unitaire)", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v1")).thenReturn(java.util.Optional.of(veh("v1", "u1@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v1", authNoEmail("USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-09 véhicule introuvable -> null", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("absent")).thenReturn(java.util.Optional.empty());
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("absent", auth("admin@mail", "ADMIN")))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-10 liste user appelle findByClientMailClient", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findByClientMailClient("u1@mail")).thenReturn(List.of());
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getDossiersAsync(auth("u1@mail", "USER")).collectList())
                    .assertNext(list -> assertTrue(list.isEmpty()))
                    .verifyComplete();
            verify(repo, times(1)).findByClientMailClient("u1@mail");
        }));
        tests.add(DynamicTest.dynamicTest("VEH-11 liste admin n'appelle pas findByClientMailClient", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findAll()).thenReturn(List.of());
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getDossiersAsync(auth("admin@mail", "ADMIN")).collectList())
                    .assertNext(list -> assertTrue(list.isEmpty()))
                    .verifyComplete();
            verify(repo, never()).findByClientMailClient(any());
        }));
        tests.add(DynamicTest.dynamicTest("VEH-12 admin lit véhicule sans propriétaire", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v3")).thenReturn(java.util.Optional.of(veh("v3", null)));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v3", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("v3", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-13 user lit véhicule sans propriétaire -> 403", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v3")).thenReturn(java.util.Optional.of(veh("v3", null)));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getVehiculeByIdAsync("v3", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("VEH-14 methode legacy getDocumentByIdAsync fonctionne", () -> {
            VehiculeRepository repo = mock(VehiculeRepository.class);
            when(repo.findById("v1")).thenReturn(java.util.Optional.of(veh("v1", "u1@mail")));
            VehiculeQueryController c = new VehiculeQueryController(repo);
            StepVerifier.create(c.getDocumentByIdAsync("v1"))
                    .assertNext(dto -> assertEquals("v1", dto.getId()))
                    .verifyComplete();
        }));

        // ===========================
        // 3) Dossier access (12)
        // ===========================
        tests.add(DynamicTest.dynamicTest("DOS-01 admin voit toute la liste", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierList(gateway, new DossierListResponse(List.of(
                    dossier("ds1", "u1@mail"), dossier("ds2", "u2@mail"), dossier("ds3", null)
            )));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossiersAsync(auth("admin@mail", "ADMIN")).collectList())
                    .assertNext(list -> assertEquals(3, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-02 user voit seulement ses dossiers", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierList(gateway, new DossierListResponse(List.of(
                    dossier("ds1", "u1@mail"), dossier("ds2", "u2@mail"), dossier("ds3", "u1@mail")
            )));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossiersAsync(auth("u1@mail", "USER")).collectList())
                    .assertNext(list -> assertEquals(2, list.size()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-03 user sans email interdit (liste)", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierList(gateway, new DossierListResponse(List.of()));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            java.util.concurrent.CompletionException ex = assertThrows(java.util.concurrent.CompletionException.class,
                    () -> c.getDossiersAsync(authNoEmail("USER")));
            assertTrue(ex.getCause() instanceof ResponseStatusException);
            assertEquals(403, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
        }));
        tests.add(DynamicTest.dynamicTest("DOS-04 auth null interdite (liste)", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierList(gateway, new DossierListResponse(List.of()));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            java.util.concurrent.CompletionException ex = assertThrows(java.util.concurrent.CompletionException.class,
                    () -> c.getDossiersAsync(null));
            assertTrue(ex.getCause() instanceof ResponseStatusException);
            assertEquals(403, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
        }));
        tests.add(DynamicTest.dynamicTest("DOS-05 admin lit dossier autre client", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds2", "u2@mail"));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds2", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("ds2", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-06 user lit son dossier", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds1", "u1@mail"));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds1", auth("u1@mail", "USER")))
                    .assertNext(dto -> assertEquals("ds1", dto.getId()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-07 user lit dossier autre client -> 403", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds2", "u2@mail"));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds2", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-08 user sans email interdit (unitaire)", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds1", "u1@mail"));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds1", authNoEmail("USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-09 auth null interdite (unitaire)", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds1", "u1@mail"));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds1", null))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-10 dossier introuvable -> null", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, null);
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("absent", auth("admin@mail", "ADMIN")))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-11 dossier sans client invisible pour user", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds3", null));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds3", auth("u1@mail", "USER")))
                    .expectErrorSatisfies(e -> assertEquals(403, ((ResponseStatusException) e).getStatusCode().value()))
                    .verify();
        }));
        tests.add(DynamicTest.dynamicTest("DOS-12 dossier sans client visible admin", () -> {
            QueryGateway gateway = mock(QueryGateway.class);
            stubDossierById(gateway, dossier("ds3", null));
            DossierQueryController c = new DossierQueryController(gateway, mock(fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository.class));
            StepVerifier.create(c.getDossierByIdAsync("ds3", auth("admin@mail", "ADMIN")))
                    .assertNext(dto -> assertEquals("ds3", dto.getId()))
                    .verifyComplete();
        }));

        // ===========================
        // 4) Creation & annotations (10)
        // ===========================
        tests.add(DynamicTest.dynamicTest("CRT-01 PreAuthorize client create = ADMIN", () ->
                assertEquals("hasRole('ADMIN')", preAuth(ClientCommandController.class, "createClientAsync"))));
        tests.add(DynamicTest.dynamicTest("CRT-02 PreAuthorize vehicule create = ADMIN/USER", () ->
                assertEquals("hasAnyRole('ADMIN', 'USER')", preAuth(VehiculeCommandController.class, "createClientAsync"))));
        tests.add(DynamicTest.dynamicTest("CRT-03 PreAuthorize document create = ADMIN/USER", () ->
                assertEquals("hasAnyRole('ADMIN', 'USER')", preAuth(DocumentCommandController.class, "createClientAsync"))));
        tests.add(DynamicTest.dynamicTest("CRT-04 PreAuthorize dossier create = ADMIN", () ->
                assertEquals("hasRole('ADMIN')", preAuth(DossierCommandController.class, "createClientAsync"))));

        tests.add(DynamicTest.dynamicTest("CRT-05 document create USER impose son clientId", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            ClientRepository clientRepo = mock(ClientRepository.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore, clientRepo);
            DocumentCommandDTO dto = new DocumentCommandDTO();
            dto.setNomDocument("N");
            when(clientRepo.findByMailClient("u1@mail")).thenReturn(java.util.Optional.of(clientEntity("cli-1", "u1@mail")));
            when(service.createDocument(any())).thenAnswer(inv -> CompletableFuture.completedFuture(inv.getArgument(0)));

            StepVerifier.create(c.createClientAsync(dto, auth("u1@mail", "USER")))
                    .assertNext(r -> assertEquals(201, r.getStatusCode().value()))
                    .verifyComplete();

            ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);
            verify(service).createDocument(captor.capture());
            assertEquals("cli-1", captor.getValue().getClientId());
        }));
        tests.add(DynamicTest.dynamicTest("CRT-06 document create USER override clientId fourni", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            ClientRepository clientRepo = mock(ClientRepository.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore, clientRepo);
            DocumentCommandDTO dto = new DocumentCommandDTO();
            dto.setClientId("autre");
            when(clientRepo.findByMailClient("u1@mail")).thenReturn(java.util.Optional.of(clientEntity("cli-1", "u1@mail")));
            when(service.createDocument(any())).thenAnswer(inv -> CompletableFuture.completedFuture(inv.getArgument(0)));

            StepVerifier.create(c.createClientAsync(dto, auth("u1@mail", "USER")))
                    .assertNext(r -> assertEquals(201, r.getStatusCode().value()))
                    .verifyComplete();

            ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);
            verify(service).createDocument(captor.capture());
            assertEquals("cli-1", captor.getValue().getClientId());
        }));
        tests.add(DynamicTest.dynamicTest("CRT-07 document create ADMIN conserve clientId fourni", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            ClientRepository clientRepo = mock(ClientRepository.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore, clientRepo);
            DocumentCommandDTO dto = new DocumentCommandDTO();
            dto.setClientId("cli-x");
            when(clientRepo.findByMailClient("admin@mail")).thenReturn(java.util.Optional.empty());
            when(service.createDocument(any())).thenAnswer(inv -> CompletableFuture.completedFuture(inv.getArgument(0)));

            StepVerifier.create(c.createClientAsync(dto, auth("admin@mail", "ADMIN")))
                    .assertNext(r -> assertEquals(201, r.getStatusCode().value()))
                    .verifyComplete();

            ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);
            verify(service).createDocument(captor.capture());
            assertEquals("cli-x", captor.getValue().getClientId());
        }));
        tests.add(DynamicTest.dynamicTest("CRT-08 document create USER inconnu -> 403", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            ClientRepository clientRepo = mock(ClientRepository.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore, clientRepo);
            when(clientRepo.findByMailClient("ghost@mail")).thenReturn(java.util.Optional.empty());

            StepVerifier.create(c.createClientAsync(new DocumentCommandDTO(), auth("ghost@mail", "USER")))
                    .assertNext(r -> assertEquals(403, r.getStatusCode().value()))
                    .verifyComplete();

            verify(service, never()).createDocument(any());
        }));
        tests.add(DynamicTest.dynamicTest("CRT-09 document create surcharge legacy success", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore);
            DocumentCommandDTO dto = new DocumentCommandDTO();
            when(service.createDocument(any())).thenReturn(CompletableFuture.completedFuture(dto));

            StepVerifier.create(c.createClientAsync(dto))
                    .assertNext(r -> assertEquals(201, r.getStatusCode().value()))
                    .verifyComplete();
        }));
        tests.add(DynamicTest.dynamicTest("CRT-10 document create surcharge legacy erreur", () -> {
            DocumentCommandService service = mock(DocumentCommandService.class);
            EventStore eventStore = mock(EventStore.class);
            DocumentCommandController c = new DocumentCommandController(service, eventStore);
            CompletableFuture<DocumentCommandDTO> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("boom"));
            when(service.createDocument(any())).thenReturn(failed);

            StepVerifier.create(c.createClientAsync(new DocumentCommandDTO()))
                    .assertNext(r -> assertEquals(500, r.getStatusCode().value()))
                    .verifyComplete();
        }));

        assertEquals(50, tests.size(), "La matrice RBAC doit contenir exactement 50 tests");
        return tests;
    }

    private static String preAuth(Class<?> type, String methodName) {
        for (Method m : type.getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            PreAuthorize ann = m.getAnnotation(PreAuthorize.class);
            if (ann != null) {
                return ann.value();
            }
        }
        fail("@PreAuthorize introuvable sur " + type.getSimpleName() + "." + methodName);
        return null;
    }

    private static Authentication auth(String email, String role) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of(role)));
        claims.put("email", email);
        Jwt jwt = new Jwt("token-" + role + "-" + email,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
        return new JwtAuthenticationToken(jwt);
    }

    private static Authentication authNoEmail(String role) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of(role)));
        Jwt jwt = new Jwt("token-" + role,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
        return new JwtAuthenticationToken(jwt);
    }

    private static Document doc(String id, String ownerEmail) {
        Document d = new Document();
        d.setId(id);
        d.setNomDocument("DOC-" + id);
        d.setTypeDocument(TypeDocument.DEVIS);
        d.setDateCreationDocument(Instant.now());
        d.setDateModificationDocument(Instant.now());
        d.setDocumentStatus(DocumentStatusDTO.CREATED);
        if (ownerEmail != null) {
            Client c = new Client();
            c.setId("cli-" + ownerEmail);
            c.setMailClient(ownerEmail);
            d.setClient(c);
        }
        return d;
    }

    private static Vehicule veh(String id, String ownerEmail) {
        Vehicule v = new Vehicule();
        v.setId(id);
        v.setImmatriculationVehicule("IMM-" + id);
        v.setDateMiseEnCirculationVehicule(Instant.now());
        v.setVehiculeStatus(VehiculeStatus.EN_CIRCULATION);
        if (ownerEmail != null) {
            Client c = new Client();
            c.setId("cli-" + ownerEmail);
            c.setMailClient(ownerEmail);
            v.setClient(c);
        }
        return v;
    }

    private static DossierQueryDTO dossier(String id, String ownerEmail) {
        DossierQueryDTO dto = new DossierQueryDTO();
        dto.setId(id);
        if (ownerEmail != null) {
            ClientQueryDTO c = new ClientQueryDTO();
            c.setId("cli-" + ownerEmail);
            c.setMailClient(ownerEmail);
            dto.setClient(c);
        }
        return dto;
    }

    private static Client clientEntity(String id, String email) {
        Client c = new Client();
        c.setId(id);
        c.setMailClient(email);
        return c;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void stubDossierList(QueryGateway gateway, DossierListResponse response) {
        when(gateway.query(any(GetAllDossiersDTO.class), any(ResponseType.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(response));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void stubDossierById(QueryGateway gateway, DossierQueryDTO response) {
        when(gateway.query(any(GetDossierDTO.class), any(ResponseType.class)))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(response));
    }
}

