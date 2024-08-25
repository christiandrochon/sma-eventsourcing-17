package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DossierStatusThymDTO {
    OUVERT, CLOTURE, REOUVERT, ANNULE, REFUSE, MODIFIE, VALIDE, REJET, ACCEPTE, TRAITE, ENVOYE, RECU, RETOURNE, ARCHIVE, DESARCHIVE, SUPPRIME, RESTAURE, PURGE;
    
    /**
     * Cree une instance de l'enum à partir de la valeur. Permet egalement de prendre en charge la casse des valeurs.
     *
     * @param value la valeur de l'enum
     * @return l'enum correspondant à la valeur
     */
    @JsonCreator
    public static DossierStatusThymDTO forValue(String value) {
        for(DossierStatusThymDTO status : values()) {
            if(status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return OUVERT; // Default value for unknown enum values
    }
    
    /**
     * Permet de retourner le libellé de l'enum
     *
     * @return le libellé de l'enum
     */
    @JsonValue
    public String getLabel() {
        return this.name();
    }
    
    /**
     * Permet de retourner la valeur par défaut
     *
     * @return la valeur par défaut
     */
    public static DossierStatusThymDTO valeurDossierStatutParDefaut() {
        return OUVERT; // Retourne la valeur par défaut
    }
}
