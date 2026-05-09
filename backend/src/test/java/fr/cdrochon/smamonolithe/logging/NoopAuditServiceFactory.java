package fr.cdrochon.smamonolithe.logging;

import fr.cdrochon.smamonolithe.audit.domain.AuditEventRecord;
import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.mockito.Mockito.mock;

/**
 * Helper de test : fournit un AuditService no-op (sans base de données)
 * pour les tests unitaires du filtre {@link TechnicalRequestWebFilter}.
 */
final class NoopAuditServiceFactory {

    private NoopAuditServiceFactory() {}

    /**
     * Crée un AuditService qui ne fait rien (pas de BDD en test unitaire).
     * L'insertion est remplacée par un no-op.
     */
    static AuditService noop() {
        NamedParameterJdbcTemplate mockJdbc = mock(NamedParameterJdbcTemplate.class);
        return new AuditService(mockJdbc) {
            @Override
            public void record(AuditEventRecord event) {
                // No-op en test : on ne veut pas de connexion PostgreSQL
            }
        };
    }
}

