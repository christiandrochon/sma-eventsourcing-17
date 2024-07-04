package fr.cdrochon.smamonolithe.client.query.entities;

import fr.cdrochon.smamonolithe.client.command.enums.ClientStatus;
import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;


@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class Client {
    
    @Id
    private String id;
    
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    
    @Embedded
    private AdresseClient adresse;
    @Enumerated
    private ClientStatus clientStatus;
}
