package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehiculeStatusDTO {
    EN_ATTENTE,
    EN_CIRCULATION,
    HORS_SERVICE,
    VENDU,
    VOLE;
    
    @JsonCreator
    public static VehiculeStatusDTO forValue(String value) {
        for (VehiculeStatusDTO status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return EN_ATTENTE; // Default value for unknown enum values
    }
    @JsonValue
    public String getLabel() {
        return this.name();
    }
    
}
