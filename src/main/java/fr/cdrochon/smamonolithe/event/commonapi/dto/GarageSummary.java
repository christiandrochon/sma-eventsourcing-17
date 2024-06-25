package fr.cdrochon.smamonolithe.event.commonapi.dto;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;

import java.util.Objects;

public final class GarageSummary {
    private final String id;
    private final String nomGarage;
    private final String mailResp;
    private final GarageStatus garageStatus;
    
    public GarageSummary(String id, String nomGarage, String mailResp, GarageStatus garageStatus) {
        this.id = id;
        this.nomGarage = nomGarage;
        this.mailResp = mailResp;
        this.garageStatus = garageStatus;
    }
    
    public static GarageSummary create(String id, String nomGarage, String mailResp, GarageStatus garageStatus) {
        return new GarageSummary(id, nomGarage, mailResp, garageStatus);
    }
    
    public String id() {
        return id;
    }
    
    public String nomGarage() {
        return nomGarage;
    }
    
    public String mailResp() {
        return mailResp;
    }
    
    public GarageStatus garageStatus() {
        return garageStatus;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        GarageSummary that = (GarageSummary) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.nomGarage, that.nomGarage) &&
                Objects.equals(this.mailResp, that.mailResp) &&
                Objects.equals(this.garageStatus, that.garageStatus);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nomGarage, mailResp, garageStatus);
    }
    
    @Override
    public String toString() {
        return "GarageSummary[" +
                "id=" + id + ", " +
                "nomGarage=" + nomGarage + ", " +
                "mailResp=" + mailResp + ", " +
                "garageStatus=" + garageStatus + ']';
    }
    
}
