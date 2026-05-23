package fr.cdrochon.smamonolithe.dossier.query.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "DTO représentant un dossier dans une requête de lecture")
public class DossierQueryDTO {
    @Schema(description = "Identifiant unique du dossier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Nom du dossier", example = "Dossier de garantie véhicule")
    private String nomDossier;
    @Schema(description = "Date de création du dossier")
    private Instant dateCreationDossier;
    @Schema(description = "Date de dernière modification du dossier")
    private Instant dateModificationDossier;
    @Schema(description = "Client associé au dossier")
    private ClientQueryDTO client;
    @Schema(description = "Véhicule associé au dossier")
    private VehiculeQueryDTO vehicule;
    //enum is immutable, no need to convert it to DTO
    @Schema(description = "Statut du dossier (OUVERT, CLOTURE, ARCHIVE, etc.)", example = "OUVERT")
    private DossierStatus dossierStatus;
}
