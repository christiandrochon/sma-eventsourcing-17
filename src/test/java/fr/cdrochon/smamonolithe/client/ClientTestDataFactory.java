package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;

public class ClientTestDataFactory {

    public static ClientAdresseDTO sampleAdresseDTO() {
        return ClientAdresseDTO.builder()
                .numeroDeRue("12")
                .rue("Rue de la Paix")
                .complementAdresse("Apt 3")
                .cp("75001")
                .ville("Paris")
                .pays(Pays.FRANCE)
                .build();
    }

    public static AdresseClient sampleAdresseClient() {
        return AdresseClient.builder()
                .numeroDeRue("12")
                .rue("Rue de la Paix")
                .complementAdresse("Apt 3")
                .cp("75001")
                .ville("Paris")
                .pays(Pays.FRANCE)
                .build();
    }

    public static ClientCommandDTO sampleClientCommandDTO() {
        return ClientCommandDTO.builder()
                .id("client-uuid-1")
                .nomClient("Dupont")
                .prenomClient("Jean")
                .mailClient("jean.dupont@mail.com")
                .telClient("0601020304")
                .adresse(sampleAdresseDTO())
                .clientStatus(ClientStatus.ACTIF)
                .build();
    }

    public static ClientCreatedEvent sampleClientCreatedEvent() {
        return new ClientCreatedEvent(
                "client-uuid-1",
                "Dupont",
                "Jean",
                "jean.dupont@mail.com",
                "0601020304",
                sampleAdresseClient(),
                ClientStatus.ACTIF
        );
    }

    public static Client sampleClient() {
        return Client.builder()
                .id("client-uuid-1")
                .nomClient("Dupont")
                .prenomClient("Jean")
                .mailClient("jean.dupont@mail.com")
                .telClient("0601020304")
                .adresse(sampleAdresseClient())
                .clientStatus(ClientStatus.ACTIF)
                .build();
    }
}