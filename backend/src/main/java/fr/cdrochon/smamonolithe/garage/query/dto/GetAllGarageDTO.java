package fr.cdrochon.smamonolithe.garage.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sert à effectuer une query.
 * <p>
 * Si il y a des params de pagination, c'st ici qu'on les mets, etc
 */
@Schema(description = "Requête pour récupérer tous les garages")
public class GetAllGarageDTO {
}
