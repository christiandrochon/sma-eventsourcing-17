package fr.cdrochon.smamonolithe.event.commonapi.dto;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;

public record GarageSummary(String id, String nomGarage, String mailResp, GarageStatus garageStatus) {
    
    public static GarageSummary create(String id, String nomGarage, String mailResp, GarageStatus garageStatus) {
        return new GarageSummary(id, nomGarage, mailResp, garageStatus);
    }
}
