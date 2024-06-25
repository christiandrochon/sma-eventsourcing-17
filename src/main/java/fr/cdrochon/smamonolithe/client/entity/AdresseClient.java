package fr.cdrochon.smamonolithe.client.entity;


import lombok.*;

import javax.persistence.Embeddable;


@Embeddable
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class AdresseClient {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
