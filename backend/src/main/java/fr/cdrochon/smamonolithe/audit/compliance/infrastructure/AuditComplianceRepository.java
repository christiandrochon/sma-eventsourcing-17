package fr.cdrochon.smamonolithe.audit.compliance.infrastructure;

import fr.cdrochon.smamonolithe.audit.compliance.api.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class AuditComplianceRepository {

    // Colonnes réutilisées dans les RowMapper / paramètres SQL -> éviter les littéraux dupliqués
    private static final String COL_STATUS = "status";
    private static final String COL_SCORE = "score";


    private static final RowMapper<AuditExpectationItem> EXPECTATION_MAPPER = new RowMapper<>() {
        @Override
        public AuditExpectationItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditExpectationLatestCheck latest = null;
            Long checkId = (Long) rs.getObject("check_id");
            if (checkId != null) {
                latest = new AuditExpectationLatestCheck(
                        checkId,
                        toInstant(rs.getObject("checked_at")),
                        rs.getString("checked_by"),
                        rs.getString(COL_STATUS),
                        (Integer) rs.getObject(COL_SCORE),
                        rs.getString("findings"),
                        rs.getString("remediation_plan"),
                        rs.getString("evidence_uri")
                );
            }

            return new AuditExpectationItem(
                    rs.getString("code"),
                    rs.getString("domain"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("independent_evidence"),
                    rs.getString("legal_reference"),
                    rs.getString("expected_frequency"),
                    rs.getBoolean("enabled"),
                    toInstant(rs.getObject("created_at")),
                    latest
            );
        }
    };

    private static final RowMapper<AuditExpectationCheckEntry> CHECK_MAPPER = (rs, rowNum) -> new AuditExpectationCheckEntry(
            rs.getLong("id"),
            rs.getString("expectation_code"),
            toInstant(rs.getObject("checked_at")),
            rs.getString("checked_by"),
            rs.getString(COL_STATUS),
            (Integer) rs.getObject(COL_SCORE),
            rs.getString("scope"),
            rs.getString("findings"),
            rs.getString("remediation_plan"),
            rs.getObject("due_date", java.time.LocalDate.class),
            rs.getString("evidence_uri"),
            (Integer) rs.getObject("cross_garage_sample_size"),
            rs.getString("inserted_from")
    );

    private final NamedParameterJdbcTemplate auditJdbc;

    public AuditComplianceRepository(@Qualifier("auditJdbcTemplate") NamedParameterJdbcTemplate auditJdbc) {
        this.auditJdbc = auditJdbc;
    }

    public List<AuditExpectationItem> listExpectations(String domain, boolean enabledOnly) {
        String sql = """
                SELECT
                    e.code, e.domain, e.title, e.description, e.independent_evidence,
                    e.legal_reference, e.expected_frequency, e.enabled, e.created_at,
                    l.check_id, l.checked_at, l.checked_by, l.status, l.score,
                    l.findings, l.remediation_plan, l.evidence_uri
                FROM audit_expectations e
                LEFT JOIN audit_expectations_latest l ON l.code = e.code
                WHERE (CAST(:domain AS VARCHAR) IS NULL OR e.domain = CAST(:domain AS VARCHAR))
                  AND (:enabledOnly = FALSE OR e.enabled = TRUE)
                ORDER BY e.domain, e.code
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("domain", domain)
                .addValue("enabledOnly", enabledOnly);

        return auditJdbc.query(sql, params, EXPECTATION_MAPPER);
    }

    public Optional<AuditExpectationItem> findExpectation(String code) {
        String sql = """
                SELECT
                    e.code, e.domain, e.title, e.description, e.independent_evidence,
                    e.legal_reference, e.expected_frequency, e.enabled, e.created_at,
                    l.check_id, l.checked_at, l.checked_by, l.status, l.score,
                    l.findings, l.remediation_plan, l.evidence_uri
                FROM audit_expectations e
                LEFT JOIN audit_expectations_latest l ON l.code = e.code
                WHERE e.code = :code
                """;

        List<AuditExpectationItem> rows = auditJdbc.query(sql, new MapSqlParameterSource("code", code), EXPECTATION_MAPPER);
        return rows.stream().findFirst();
    }

    public List<AuditExpectationCheckEntry> listChecks(String code, int limit) {
        String sql = """
                SELECT id, expectation_code, checked_at, checked_by, status, score, scope,
                       findings, remediation_plan, due_date, evidence_uri,
                       cross_garage_sample_size, inserted_from
                FROM audit_expectation_checks
                WHERE expectation_code = :code
                ORDER BY checked_at DESC
                LIMIT :limit
                """;

        return auditJdbc.query(sql,
                new MapSqlParameterSource().addValue("code", code).addValue("limit", limit),
                CHECK_MAPPER);
    }

    public AuditExpectationCheckEntry insertCheck(String code, CreateAuditExpectationCheckRequest request) {
        String sql = """
                INSERT INTO audit_expectation_checks
                    (expectation_code, checked_by, status, score, scope,
                     findings, remediation_plan, due_date, evidence_uri,
                     cross_garage_sample_size, inserted_from)
                VALUES
                    (:expectationCode, :checkedBy, :status, :score, :scope,
                     :findings, :remediationPlan, :dueDate, :evidenceUri,
                     :crossGarageSampleSize, :insertedFrom)
                RETURNING id, expectation_code, checked_at, checked_by, status, score, scope,
                          findings, remediation_plan, due_date, evidence_uri,
                          cross_garage_sample_size, inserted_from
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("expectationCode", code)
                .addValue("checkedBy", request.checkedBy())
                .addValue(COL_STATUS, request.status().name())
                .addValue(COL_SCORE, request.score())
                .addValue("scope", request.scope())
                .addValue("findings", request.findings())
                .addValue("remediationPlan", request.remediationPlan())
                .addValue("dueDate", request.dueDate())
                .addValue("evidenceUri", request.evidenceUri())
                .addValue("crossGarageSampleSize", request.crossGarageSampleSize())
                .addValue("insertedFrom", request.insertedFrom() != null ? request.insertedFrom() : "MANUAL");

        return auditJdbc.queryForObject(sql, params, CHECK_MAPPER);
    }

    public AuditComplianceDashboard dashboard() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM audit_expectations WHERE enabled = TRUE) AS enabled_expectations,
                    (SELECT COUNT(*) FROM audit_expectations_latest WHERE check_id IS NOT NULL) AS expectations_with_any_check,
                    (SELECT COUNT(*) FROM audit_expectations_latest WHERE status = 'COMPLIANT') AS compliant_latest,
                    (SELECT COUNT(*) FROM audit_expectations_latest WHERE status = 'PARTIAL') AS partial_latest,
                    (SELECT COUNT(*) FROM audit_expectations_latest WHERE status = 'NON_COMPLIANT') AS non_compliant_latest,
                    (SELECT COUNT(*) FROM audit_expectations_latest WHERE status = 'NOT_APPLICABLE') AS not_applicable_latest,
                    (SELECT COUNT(*) FROM audit_events WHERE action = 'ACCESS_DENIED' AND event_time >= now() - INTERVAL '30 days') AS access_denied_30d,
                    (SELECT COUNT(*) FROM audit_events WHERE action = 'ANOMALY' AND event_time >= now() - INTERVAL '30 days') AS anomalies_30d,
                    (SELECT COUNT(*) FROM audit_events WHERE cross_garage = TRUE AND event_time >= now() - INTERVAL '30 days') AS cross_garage_30d
                """;

        return auditJdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> new AuditComplianceDashboard(
                rs.getLong("enabled_expectations"),
                rs.getLong("expectations_with_any_check"),
                rs.getLong("compliant_latest"),
                rs.getLong("partial_latest"),
                rs.getLong("non_compliant_latest"),
                rs.getLong("not_applicable_latest"),
                rs.getLong("access_denied_30d"),
                rs.getLong("anomalies_30d"),
                rs.getLong("cross_garage_30d")
        ));
    }

    private static Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof java.time.Instant instant) {
            return instant;
        }
        throw new IllegalStateException("Unsupported temporal type: " + value.getClass());
    }
}

