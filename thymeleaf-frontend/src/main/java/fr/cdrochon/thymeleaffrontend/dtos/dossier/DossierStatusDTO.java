package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;

public enum DossierStatusDTO {
    OUVERT, CLOTURE, REOUVERT, ANNULE, REFUSE, MODIFIE, VALIDE, REJET, ACCEPTE, TRAITE, ENVOYE, RECU, RETOURNE, ARCHIVE, DESARCHIVE, SUPPRIME, RESTAURE, PURGE;
    
    @JsonCreator
    public static DossierStatusDTO forValue(String value) {
        for (DossierStatusDTO status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return OUVERT; // Default value for unknown enum values
    }
    @JsonValue
    public String getLabel() {
        return this.name();
    }
    
    public static DossierStatusDTO valeurDossierStatutParDefaut() {
        return OUVERT; // Retourne la valeur par défaut
    }
}
