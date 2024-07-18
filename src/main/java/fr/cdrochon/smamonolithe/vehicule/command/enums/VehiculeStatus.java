package fr.cdrochon.smamonolithe.vehicule.command.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VehiculeStatus {
    
    EN_ATTENTE,
    EN_CIRCULATION,
    HORS_SERVICE,
    VENDU,
    VOLE;
    @JsonCreator
    public static VehiculeStatus forValue(String value) {
        for (VehiculeStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return EN_ATTENTE; // Default value for unknown enum values
    }
}
