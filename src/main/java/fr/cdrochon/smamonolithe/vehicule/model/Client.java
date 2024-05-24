package fr.cdrochon.smamonolithe.vehicule.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class Client {
    
    private Long id;
    
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClient adresseClient;
}
