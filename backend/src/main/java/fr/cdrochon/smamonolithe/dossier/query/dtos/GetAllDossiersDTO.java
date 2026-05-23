package fr.cdrochon.smamonolithe.dossier.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message de query Axon pour recuperer tous les dossiers.
 */
@Schema(description = "Requête pour récupérer tous les dossiers")
public record GetAllDossiersDTO() {
}
