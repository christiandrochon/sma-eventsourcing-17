package fr.cdrochon.smamonolithe.dossier.command.dtos;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

/**
 * Permet de faire le lien entre les services command de l'appli et le monde exteieur
 *<p>
 * Les noms des attributs doivent correspondre à ceux du dto
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "DTO utilisé pour les commandes liées aux dossiers")
public class DossierCommandDTO {

    @Schema(description = "Identifiant unique du dossier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Nom du dossier", example = "Dossier de garantie véhicule")
    private String nomDossier;
    @Schema(description = "Date de création du dossier")
    private Instant dateCreationDossier;
    @Schema(description = "Date de dernière modification du dossier")
    private Instant dateModificationDossier;
    @Schema(description = "Client associé au dossier")
    private ClientCommandDTO client;
    @Schema(description = "Véhicule associé au dossier")
    private VehiculeCommandDTO vehicule;
    // Un enum est immutable, pas besoin de le convertir en DTO
    @Schema(description = "Statut du dossier", example = "OUVERT")
    private DossierStatus dossierStatus;
    @Schema(description = "Identifiant de l'utilisateur propriétaire du dossier", example = "user123")
    private String userId;
}
