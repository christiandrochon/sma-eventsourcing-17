package fr.cdrochon.smamonolithe.client.query.entities;

import fr.cdrochon.smamonolithe.client.command.enums.ClientStatus;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import lombok.*;

import javax.persistence.*;


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
    
    @OneToOne(mappedBy = "client")
    private Dossier dossier;
}
