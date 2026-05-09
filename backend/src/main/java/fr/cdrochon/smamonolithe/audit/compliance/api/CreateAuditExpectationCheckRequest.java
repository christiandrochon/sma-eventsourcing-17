package fr.cdrochon.smamonolithe.audit.compliance.api;

import fr.cdrochon.smamonolithe.audit.compliance.domain.ComplianceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Commande HTTP pour ajouter une verification d'attente d'audit.
 */
public record CreateAuditExpectationCheckRequest(
        @NotBlank
        @Size(max = 255)
        String checkedBy,

        @NotNull
        ComplianceStatus status,

        @Min(0)
        @Max(100)
        Integer score,

        @Size(max = 255)
        String scope,

        String findings,

        String remediationPlan,

        LocalDate dueDate,

        @Size(max = 1024)
        String evidenceUri,

        @Min(0)
        Integer crossGarageSampleSize,

        @Size(max = 100)
        String insertedFrom
) {
}

