package fr.cdrochon.smamonolithe.audit.infrastructure;

import fr.cdrochon.smamonolithe.audit.domain.AuditEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Service d'audit RGPD.
 *
 * <p>Responsabilité unique : insérer un événement d'audit dans la table {@code audit_events}
 * de la base PostgreSQL dédiée {@code audit}.</p>
 *
 * <h3>Garanties RGPD :</h3>
 * <ul>
 *   <li>INSERT uniquement – l'utilisateur PostgreSQL n'a pas de droit UPDATE/DELETE sur la table.</li>
 *   <li>L'insertion est asynchrone (@Async) pour ne pas bloquer le thread métier.</li>
 *   <li>En cas d'erreur (base indisponible), l'exception est loguée mais ne propage pas.</li>
 *   <li>L'objet {@link AuditEventRecord} est immuable (Java record).</li>
 * </ul>
 *
 * <h3>Utilisation :</h3>
 * <pre>
 * auditService.record(
 *     AuditEventRecord.builder()
 *         .actor("utilisateur@garage-a.fr")
 *         .actorGarage("GARAGE_A")
 *         .action(AuditAction.READ)
 *         .resource(AuditResource.VEHICULE)
 *         .resourceId("abc-123")
 *         .garageId("GARAGE_B")         // différent → crossGarage = true
 *         .crossGarage(true)
 *         .result(AuditResult.SUCCESS)
 *         .httpMethod("GET")
 *         .httpPath("/queries/vehicules/abc-123")
 *         .httpStatus(200)
 *         .ipAddress("192.168.1.10")
 *         .build()
 * );
 * </pre>
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private static final String INSERT_AUDIT = """
            INSERT INTO audit_events
                (event_time, actor, actor_garage, action, resource, resource_id,
                 garage_id, cross_garage, reason, result,
                 http_method, http_path, http_status,
                 ip_address, user_agent, details)
            VALUES
                (:eventTime, :actor, :actorGarage, :action, :resource, :resourceId,
                 :garageId, :crossGarage, :reason, :result,
                 :httpMethod, :httpPath, :httpStatus,
                 :ipAddress, :userAgent, :details)
            """;

    private final NamedParameterJdbcTemplate auditJdbc;

    public AuditService(@Qualifier("auditJdbcTemplate") NamedParameterJdbcTemplate auditJdbc) {
        this.auditJdbc = auditJdbc;
    }

    /**
     * Enregistre un événement d'audit de façon asynchrone.
     * L'appel est non bloquant : il ne ralentit pas le traitement métier.
     *
     * @param event l'événement à enregistrer (immuable)
     */
    @Async
    public void record(AuditEventRecord event) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("eventTime",   Timestamp.from(event.eventTime()))
                    .addValue("actor",       event.actor())
                    .addValue("actorGarage", event.actorGarage())
                    .addValue("action",      event.action().name())
                    .addValue("resource",    event.resource().name())
                    .addValue("resourceId",  event.resourceId())
                    .addValue("garageId",    event.garageId())
                    .addValue("crossGarage", event.crossGarage())
                    .addValue("reason",      event.reason())
                    .addValue("result",      event.result().name())
                    .addValue("httpMethod",  event.httpMethod())
                    .addValue("httpPath",    event.httpPath())
                    .addValue("httpStatus",  event.httpStatus())
                    .addValue("ipAddress",   event.ipAddress())
                    .addValue("userAgent",   event.userAgent())
                    .addValue("details",     event.details());

            auditJdbc.update(INSERT_AUDIT, params);

            log.debug("AUDIT recorded: actor={} action={} resource={}/{} result={}",
                    event.actor(), event.action(), event.resource(), event.resourceId(), event.result());

        } catch (Exception ex) {
            // L'audit ne doit JAMAIS faire échouer le traitement métier
            log.error("AUDIT_WRITE_ERROR: impossible d'enregistrer l'événement d'audit - actor={} action={} error={}",
                    event.actor(), event.action(), ex.getMessage());
        }
    }
}

