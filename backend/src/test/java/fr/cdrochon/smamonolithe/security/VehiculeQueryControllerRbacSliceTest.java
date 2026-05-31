package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.controllers.VehiculeQueryController;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Test slice WebFlux sur les endpoints vehicules pour verifier RBAC et
 * statuts HTTP sans demarrage end-to-end.
 */
@WebFluxTest(controllers = VehiculeQueryController.class, properties = {
        "app.security.enabled=true",
        "app.security.require-authenticated-all=false",
        "app.security.audit-endpoints-authenticated=true",
        "app.security.audit-required-roles=ADMIN,AUDITOR",
        "app.security.audit-writer-roles=ADMIN",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:18080/realms/sma-realm/protocol/openid-connect/certs"
})
/**
 * @Import complete le slice @WebFluxTest avec la securite reactive JWT
 * et les beans de test necessaires au contexte minimal.
 */
@Import({SecurityConfig.class, KeycloakReactiveJwtAuthenticationConverter.class, VehiculeQueryControllerRbacSliceTest.TestBeansConfig.class})
class VehiculeQueryControllerRbacSliceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @TestConfiguration
    static class TestBeansConfig {
        @Bean
        VehiculeRepository vehiculeRepository() {
            return mock(VehiculeRepository.class);
        }

        @Bean
        AuditService auditService() {
            return mock(AuditService.class);
        }
    }

    @Test
    void shouldReturn401WhenNoJwtOnVehiculesEndpoint() {
        webTestClient.get()
                .uri("/queries/vehicules")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAdminToReadVehiculeById() {
        when(vehiculeRepository.findById("veh-2")).thenReturn(Optional.of(vehicule("veh-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                                .claim("email", "admin@mail")))
                .get()
                .uri("/queries/vehicules/veh-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("veh-2");
    }

    @Test
    void shouldForbidUserWhenReadingOtherVehiculeById() {
        when(vehiculeRepository.findById("veh-2")).thenReturn(Optional.of(vehicule("veh-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/vehicules/veh-2")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnUserVehiculeListFromOwnerQuery() {
        when(vehiculeRepository.findByClientMailClient("user@mail"))
                .thenReturn(List.of(vehicule("veh-1", "user@mail"), vehicule("veh-3", "user@mail")));
        when(vehiculeRepository.findAll()).thenReturn(List.of(vehicule("veh-admin", "admin@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/vehicules")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("veh-1")
                .jsonPath("$[1].id").isEqualTo("veh-3")
                .jsonPath("$[2]").doesNotExist();
    }

    private static Vehicule vehicule(String id, String email) {
        Client client = new Client();
        client.setId("cli-" + id);
        client.setMailClient(email);

        Vehicule vehicule = new Vehicule();
        vehicule.setId(id);
        vehicule.setImmatriculationVehicule("AA-123-BB");
        vehicule.setDateMiseEnCirculationVehicule(Instant.parse("2020-01-01T00:00:00Z"));
        vehicule.setClient(client);
        return vehicule;
    }
}

