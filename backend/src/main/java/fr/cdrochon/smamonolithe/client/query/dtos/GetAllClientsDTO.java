package fr.cdrochon.smamonolithe.client.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message de query Axon pour recuperer tous les clients.
 */
@Schema(description = "Requête pour récupérer tous les clients")
public record GetAllClientsDTO() {
}
