package fr.cdrochon.smamonolithe.vehicule.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString @AllArgsConstructor
public class AdresseClient {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
    
}
