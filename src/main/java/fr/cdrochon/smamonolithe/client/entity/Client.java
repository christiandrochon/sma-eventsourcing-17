package fr.cdrochon.smamonolithe.client.entity;

import lombok.*;

import javax.persistence.*;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    
    @Embedded
    private AdresseClient adresseClient;
}
