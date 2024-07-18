package fr.cdrochon.thymeleaffrontend.dtos.client;

import lombok.Getter;
import lombok.Setter;


public class PaysForm {
    
    private  String pays;
    
    
    public PaysForm() {
        this.pays = "FRANCE";
    }
    
    public String getPays() {
        return pays;
    }
    
    public void setPays(String pays) {
        this.pays = pays;
    }
}
