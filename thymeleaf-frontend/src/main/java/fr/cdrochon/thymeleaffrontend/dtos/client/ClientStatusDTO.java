package fr.cdrochon.thymeleaffrontend.dtos.client;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientStatusDTO {
    ACTIF, INACTIF, HISTORISE;
    
    @JsonCreator
    public static ClientStatusDTO forValue(String value) {
        for (ClientStatusDTO status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ACTIF; // Default value for unknown enum values
    }
    @JsonValue
    public String getLabel() {
        return this.name();
    }
    
    /**
     * Permet de retourner la valeur par défaut
     * @return la valeur par défaut
     */
    public static ClientStatusDTO valeurClientStatutParDefaut() {
        return ACTIF; // Retourne la valeur par défaut
    }
}
