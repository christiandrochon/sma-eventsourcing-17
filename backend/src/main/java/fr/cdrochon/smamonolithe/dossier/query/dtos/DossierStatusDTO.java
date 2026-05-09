package fr.cdrochon.smamonolithe.dossier.query.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;

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
}
