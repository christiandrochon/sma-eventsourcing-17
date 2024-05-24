package fr.cdrochon.smamonolithe.client.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable @Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class AdresseClient {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
