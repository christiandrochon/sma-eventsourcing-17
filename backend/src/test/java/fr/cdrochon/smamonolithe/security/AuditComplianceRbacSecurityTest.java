package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.audit.compliance.api.AuditComplianceController;
import fr.cdrochon.smamonolithe.audit.compliance.api.AuditComplianceDashboard;
import fr.cdrochon.smamonolithe.audit.compliance.api.AuditExpectationCheckEntry;
import fr.cdrochon.smamonolithe.audit.compliance.application.AuditComplianceService;
import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = AuditComplianceController.class)
@Import({SecurityConfig.class, KeycloakReactiveJwtAuthenticationConverter.class})
@TestPropertySource(properties = {
        "app.security.enabled=true",
        "app.security.require-authenticated-all=false",
        "app.security.audit-endpoints-authenticated=true",
        "app.security.audit-required-roles=ADMIN,AUDITOR",
        "app.security.audit-writer-roles=ADMIN",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:18080/realms/sma-realm/protocol/openid-connect/certs"
})
class AuditComplianceRbacSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuditComplianceService auditComplianceService;

    @MockBean
    private AuditService auditService;

    @Test
    void shouldReturn401WhenNoJwtOnAuditDashboard() {
        webTestClient.get()
                .uri("/audit/compliance/dashboard")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAuditorToReadDashboard() {
        when(auditComplianceService.dashboard()).thenReturn(new AuditComplianceDashboard(15, 2, 1, 1, 0, 0, 3, 1, 1));

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AUDITOR")))
                .get()
                .uri("/audit/compliance/dashboard")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldDenyAuditorOnWriteEndpoint() {
        String payload = """
                {
                  "checkedBy":"cabinet-externe",
                  "status":"COMPLIANT",
                  "score":95,
                  "scope":"scope-audit"
                }
                """;

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AUDITOR")))
                .post()
                .uri("/audit/compliance/expectations/AUD_001/checks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowAdminOnWriteEndpoint() {
        when(auditComplianceService.addCheck(ArgumentMatchers.eq("AUD_001"), ArgumentMatchers.any()))
                .thenReturn(new AuditExpectationCheckEntry(
                        1L,
                        "AUD_001",
                        Instant.now(),
                        "cabinet-externe",
                        "COMPLIANT",
                        98,
                        "scope-audit",
                        "RAS",
                        "none",
                        LocalDate.now().plusDays(30),
                        "s3://evidence/file.pdf",
                        0,
                        "INDEPENDENT_AUDIT"
                ));

        String payload = """
                {
                  "checkedBy":"cabinet-externe",
                  "status":"COMPLIANT",
                  "score":98,
                  "scope":"scope-audit",
                  "findings":"RAS",
                  "remediationPlan":"none",
                  "dueDate":"2030-01-01",
                  "evidenceUri":"s3://evidence/file.pdf",
                  "crossGarageSampleSize":0,
                  "insertedFrom":"INDEPENDENT_AUDIT"
                }
                """;

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .post()
                .uri("/audit/compliance/expectations/AUD_001/checks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void shouldAllowAdminToReadExpectations() {
        when(auditComplianceService.listExpectations(null, true)).thenReturn(List.of());

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .get()
                .uri("/audit/compliance/expectations")
                .exchange()
                .expectStatus().isOk();
    }
}

