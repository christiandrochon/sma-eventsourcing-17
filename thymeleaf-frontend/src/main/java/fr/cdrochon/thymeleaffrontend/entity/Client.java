package fr.cdrochon.thymeleaffrontend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;

    @Embedded
    private Adresse adresseClient;
}
